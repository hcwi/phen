package pl.poznan.igr.service.stats;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.google.common.io.Files;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.analysis.FDAnalysisSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.analysis.AnalysisStatus;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.stats.r.ScriptRunner;
import pl.poznan.igr.service.stats.r.ScriptStatus;

// CLEAN up logging mechanisms: slf4j, log4j, *.jars

@Service
public class FDAnalysisSessionService {

    private final static Logger log = LoggerFactory.getLogger(FDAnalysisSessionService.class);


    @Autowired
    private ScriptRunner scriptRunner;

    @Transactional
    public void analyze(Context ctx) {
        if (canProceed(ctx)) {
            log.debug("Starting analysis of " + ctx );
            startAnalysis(ctx);

            UnzipSession us = ctx.getUnzipSession();
            String path = us.getUnzipPath();
            ScriptStatus status = scriptRunner.run("a", new File(path));
            if (status.errorMessage.isPresent()) {
                log.debug("Error analysing " + ctx + ": " + status.errorMessage);
                ctx.getFDAnalysisSession().setStatus(AnalysisStatus.ERROR);
                ctx.getFDAnalysisSession().setMessage(status.errorMessage.get());
            } else {
                log.debug("Done analysing of " + ctx );
                ctx.getFDAnalysisSession().setStatus(AnalysisStatus.DONE);
                ctx.setResultFile(ctx.getImportSession().getBlobFile()); // TODO: reupload new file
            }
        }
    }

    private boolean canProceed(Context ctx) {
        if (ctx.getFDAnalysisSession() == null) {
            return true;
        }

        if (ctx.getFDAnalysisSession().getStatus() == null) {
            return true;
        }

        if (ctx.getFDAnalysisSession().getStatus() == AnalysisStatus.ERROR) {
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void startAnalysis(Context ctx) {
        FDAnalysisSession FDAnalysisSession = ctx.getFDAnalysisSession();
        if (FDAnalysisSession == null) {
            FDAnalysisSession = new FDAnalysisSession();
            FDAnalysisSession.setContext(ctx);
            ctx.setFDAnalysisSession(FDAnalysisSession);
        }
        FDAnalysisSession.setStatus(AnalysisStatus.IN_PROGRESS);
    }


    public void calculateStats(Context ctx) {

        UnzipSession us = ctx.getUnzipSession();

        String path = us.getUnzipPath();
        // new File(path + "/output").mkdir();

        try {
            // TODO wydzielić statystyki do osobnego watku
            // bo sie dlugo wczytuje
            // dynamiczne stona z lista analiz i odswiezanie stanu

            calculateStats(path, ctx);
            ctx.setStatus(Status.ANALYSED);

            // TODO separate -- think of moving to its own service
            // TODO get result file name, set in statsSession and put into DB
            // TODO manage multiple statistics files
            // File dir = new File(path + "/output");
            File dir = new File(path);
            String[] list = dir.list(new SuffixFileFilter("stat.txt"));
            String fname = path + "/" + list[0];
            log.info("Found stats file name: " + fname);
            final File f = new File(fname);
            byte[] content = Files.toByteArray(f);

            // CLEAN redesign blob creation, here and import service
            final BlobFile blobFile = new BlobFile();
            blobFile.setContent(content);
            blobFile.setFileName(fname);

            FDAnalysisSession ss = new FDAnalysisSession();
            // ss.setBlobFile(blobFile);
            ss.setContext(ctx);

            ctx.setFDAnalysisSession(ss);
            ctx.setStatus(Status.ANALYSIS_SAVED);

        } catch (IOException e) {
            e.printStackTrace();
            ctx.setStatus(Status.ANALYSIS_FAILED);
            ctx.setStatusMessage("Unexpected IO exception has occured while reading and/or analysing files.");
        } catch (RException e) {
            // TODO show exception comment to the user
            e.printStackTrace();
            ctx.setStatus(Status.ANALYSIS_FAILED);
            ctx.setStatusMessage(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            ctx.setStatus(Status.ANALYSIS_FAILED);
            ctx.setStatusMessage("Unexpected Interruped exception has occured while reading and/or analysing files.");
        }
    }

    public void calculateStats(String workingDir, Context ctx)
            throws InterruptedException, IOException {

        String rHome = System.getenv("R_HOME");
        if (rHome == null) {
            throw new RException(
                    "Environmental variable R_HOME is missing. Analysis failed.");
        }
        String exe = rHome + "Rscript";
        if (System.getProperty("os.name").startsWith("Windows")) {
            exe += ".exe";
        }
        log.info("R exe = " + exe);
        boolean can = new File(exe).canExecute();
        log.debug("can execute = " + can);
        if (!can) {
            throw new RException("Cannot run R executable at " + exe
                    + ". Analysis failed.");
        }

        URL scriptUrl = this.getClass().getClassLoader()
                .getResource("analyse.R");
        if (scriptUrl == null) {
            throw new RException("Couldn't find script analyse.R at "
                    + scriptUrl + ". Analysis failed.");
        }

        String script = scriptUrl.getFile();
        if (System.getProperty("os.name").startsWith("Windows")) {
            script = script.substring(1);
        }
        log.info("script = " + script);
        log.info("working dir = " + workingDir);

        if (!System.getProperty("os.name").startsWith("Windows")) {
            Process chmod = Runtime.getRuntime().exec(
                    "chmod 777 -R " + workingDir);
            (new RHandler(chmod.getErrorStream(), "ERROR", log, ctx)).start();
            int success = chmod.waitFor();
            if (success != 0) {
                log.error("Process exited with " + success);
                throw new RException("Changing rights to location "
                        + workingDir
                        + " failed. Probably processing fails shortly.");
            } else {
                log.info("Rights to :" + workingDir + " changed sucessfully.");
            }

            chmod = Runtime.getRuntime().exec(
                    "chmod a+x " + script);
            (new RHandler(chmod.getErrorStream(), "ERROR", log, ctx)).start();
            success = chmod.waitFor();
            if (success != 0) {
                log.error("Process exited with " + success);
                throw new RException("Changing rights to location "
                        + script
                        + " failed. Probably processing fails shortly.");
            } else {
                log.info("Rights to :" + script + " changed sucessfully.");
            }
        }

        String[] args = new String[] {exe, script, workingDir};
        log.info("Command:");
        for (String string : args) {
            log.info(string);
        }
        Process p = Runtime.getRuntime().exec(args);

        (new RHandler(p.getInputStream(), "INPUT", log, ctx)).start();
        (new RHandler(p.getErrorStream(), "ERROR", log, ctx)).start();

        int success = p.waitFor();
        if (success != 0) {
            log.error("Process exited with " + success);
            throw new RException(
                    "Analysis of the dataset failed. Probably there are errors in the processed ISA-TAB files.");
        } else {
            log.info("Process exited with " + success);
        }

    }

	/*
	 * @Override
	 *
	 * @Transactional public void calculateStats(String fileName) {
	 *
	 * //TODO rethink using JRI //cannot be initialized twice //R_HOME, R.dll in
	 * path, jri.dll in path
	 *
	 * Rengine re = new Rengine (new String[]{"--no-save"}, false, null);
	 * System.out.println("Rengine created, waiting for R");
	 *
	 * if (!re.waitForR()) { System.out.println("Cannot load R"); return; }
	 *
	 * re.assign("a", new int[]{36}); REXP ans = re.eval("sqrt(a)"); Double
	 * result = ans.asDouble(); log.info("\n\n R call result: " + result +
	 * "\n\n");
	 *
	 * re.eval("t <- read.table('test.txt')");
	 * re.eval("write(mean(t$V1), file='src//test.out.txt')");
	 * re.eval("write(getwd(), file='test.out.txt', append=T)");
	 * re.eval("sink('test.sink.txt'); print(10); sink()"); ans =
	 * re.eval("mean(t$V1)"); result = ans.asDouble();
	 * log.info("\n\n R call result: " + result + "\n\n");
	 *
	 *
	 * REXP ans = re.eval(
	 * "source('c://Users//hcwi//Documents//workspace-sts-3.2.0.RELEASE//fileup//src//main//resources//analyse.R')"
	 * ); //re.eval("write(mean(t$V1), file='source.txt');"); // //String
	 * command = "analyse('" + fileName + "')"; //ans = re.eval("mean(1:10)");
	 * log.info("\n\n R call result: " + ans + "\n\n"); //re.run();
	 *
	 * log.info(re.eval("getwd()").asString());
	 *
	 * re.end(); }
	 */

}
