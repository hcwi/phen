package pl.poznan.igr.service.stats.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.StatsSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.router.RouterService;
import pl.poznan.igr.service.stats.StatsService;

import com.google.common.io.Files;

@Service
public class StatsServiceImpl implements StatsService {

	public static final String OUT_PATH = "target/output";

	@Autowired
	RouterService routerService;
	
	private final static Logger log = LoggerFactory
			.getLogger(StatsServiceImpl.class);
	
	@Override
	public void process(Context ctx) {
		calculateStats(ctx);
		routerService.runNext(ctx);
	}
	
	@Override
	public void calculateStats(Context ctx) {

		UnzipSession us = UnzipSession.findUnzipSessionForContext(ctx);

		String path = us.getUnzipPath();
		File output = new File(path + "/output");
		output.mkdirs();

		try {
			//TODO wydzieliæ statystyki do osobnego watku, bo sie dlugo wczytuje 
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ctx.setStatus(Status.ANALYSIS_FAILED);
		}

	}

	@Override
	public void calculateStats(String fileName) throws Exception {
		
		// TODO check if R is installed - win/linux
		
		String rHome = System.getenv("R_HOME");
		if (rHome == null) {
			throw new Exception("System variable R_HOME is missing.");
		}
		String exe = rHome+"/bin/x64/Rscript.exe";
		System.out.println("==========\nR exe = "+exe);
		boolean can = new File(exe).canExecute();
		System.out.println("can execute = " + can); 
		if (!can) {
			throw new Exception("R executable not found. Analysis failed.");
		}
		
		URL scriptUrl = this.getClass().getClassLoader().getResource("analyse.R");
		if (scriptUrl == null) {
			throw new Exception("Couldn't find script analyse.R");
		}
		String script = scriptUrl.getFile().substring(1);
		System.out.println("script = " + script);
		
		String wd = fileName;
		System.out.println("working dir = " + wd);
		
		Process p;
		p = Runtime.getRuntime().exec(new String[] {exe, script, wd});
		p.getErrorStream();
		
		//TODO return error when R isn't there + when libraries are missing (they won't install on their own unless cran mirror is chosen)
		
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line;
		while ((line = br.readLine()) != null) {
			log.info(line);
		}
		p.waitFor();
		int success = p.exitValue();
		System.err.println("Process exited with " + success);
		if (success != 0) {
			// TODO recognize and handle errors
		}
		
		//TODO don't show "Download statistics" if analysis failed
	}

	/*
	 * @Override
	 * 
	 * @Transactional public void calculateStats(String fileName) {
	 * 
	 * //TODO rethink using JRI 
	 * //cannot be initialized twice 
	 * //R_HOME, R.dll in path, jri.dll in path 
	 * 
	 * Rengine re = new Rengine (new
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
	 * ); 
	 * //re.eval("write(mean(t$V1), file='source.txt');"); 
	 * // 
	 * //String command = "analyse('" + fileName + "')"; //ans = re.eval("mean(1:10)");
	 * System.err.println("\n\n R call result: " + ans + "\n\n"); 
	 * //re.run();
	 * 
	 * System.err.println(re.eval("getwd()").asString());
	 * 
	 * re.end(); }
	 */


}
