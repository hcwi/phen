package pl.poznan.igr.service.stats.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.google.common.io.Files;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.StatsSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.stats.StatsService;

@Service
public class StatsServiceImpl implements StatsService {

	public static final String OUT_PATH = "target/output";

	/*
	 * @Override
	 * 
	 * @Transactional public void calculateStats(String fileName) {
	 * 
	 * //TODO rethink using JRI //cannot be initialized twice //R_HOME, R.dll in
	 * path, jri.dll in path Rengine re = new Rengine (new
	 * String[]{"--no-save"}, false, null);
	 * System.out.println("Rengine created, waiting for R");
	 * 
	 * if (!re.waitForR()) { System.out.println("Cannot load R"); return; }
	 * 
	 * re.assign("a", new int[]{36}); REXP ans = re.eval("sqrt(a)"); Double
	 * result = ans.asDouble(); System.err.println("\n\n R call result: " +
	 * result + "\n\n");
	 * 
	 * re.eval("t <- read.table('test.txt')");
	 * re.eval("write(mean(t$V1), file='src//test.out.txt')");
	 * re.eval("write(getwd(), file='test.out.txt', append=T)");
	 * re.eval("sink('test.sink.txt'); print(10); sink()"); ans =
	 * re.eval("mean(t$V1)"); result = ans.asDouble();
	 * System.err.println("\n\n R call result: " + result + "\n\n");
	 * 
	 * 
	 * REXP ans = re.eval(
	 * "source('c://Users//hcwi//Documents//workspace-sts-3.2.0.RELEASE//fileup//src//main//resources//analyse.R')"
	 * ); //re.eval("write(mean(t$V1), file='source.txt');"); // //String
	 * command = "analyse('" + fileName + "')"; //ans = re.eval("mean(1:10)");
	 * System.err.println("\n\n R call result: " + ans + "\n\n"); //re.run();
	 * 
	 * System.err.println(re.eval("getwd()").asString());
	 * 
	 * re.end(); }
	 */

	@Override
	public void calculateStats(Context ctx) {

		UnzipSession us = UnzipSession.findUnzipSessionForContext(ctx);

		String path = us.getUnzipPath();
		File output = new File(path + "/output");
		output.mkdirs();

		try {
			calculateStats(path);
			
			ctx.setStatus(Status.ANALYSED);
			
			//TODO separate -- think of moving to its own service
			//TODO get result file name, set in statsSession and put into DB
			String fname = path + "/output/stats.txt";
			System.err.println(fname);
			final File f = new File(fname);
			byte[] content = Files.toByteArray(f);
			
			//TODO redesign blob creation, here and import service
			final BlobFile blobFile = new BlobFile();
			blobFile.setCreated(new Date());
			blobFile.setContent(content);
			blobFile.setFileName("stats.txt");
			//blobFile.persist();

			StatsSession ss = new StatsSession();
			ss.setBlobFile(blobFile);
			ss.setContext(ctx);
			ss.persist();

			ctx.setStatus(Status.ANALYSED_SAVED);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ctx.setStatus(Status.ANALYSIS_FAILED);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ctx.setStatus(Status.ANALYSIS_FAILED);
		}

	}

	@Override
	public void calculateStats(String fileName) throws IOException,
			InterruptedException {
		// TODO check if R is installed
		// TODO generalize paths
		String script = "C:/Users/Hania/Desktop/fileup/src/main/resources/analyse.R";
		String exe = "C:/Program Files/R/R-3.0.0/bin/x64/Rscript.exe";
		String wd = fileName;
		String command = exe + " " + script + " " + wd;
		System.err.println(command);
		Process p = Runtime.getRuntime().exec(command);
		// p.exitValue();
		p.waitFor();
		int success = p.exitValue();
		System.err.println("Process exited with " + success);
		if (success != 0) {
			// TODO recognize and handle errors
		}
	}

}
