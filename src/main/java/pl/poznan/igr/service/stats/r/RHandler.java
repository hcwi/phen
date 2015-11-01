package pl.poznan.igr.service.stats.r;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

import pl.poznan.igr.domain.Context;

public class RHandler extends Thread {

	InputStream is;
	String type;
	Logger log;
	Context ctx;

	public RHandler(InputStream is, String type, Logger log, Context ctx) {
		this.is = is;
		this.type = type;
		this.log = log;
		this.ctx = ctx;
	}

	public void run() {

		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("ERROR")) {
					ctx.addStatusMessage(line);
					System.err.println("SYSERR: " + line);
					log.error(line);
				}
				else {
					log.info(type + ": " + line);
				}
			}
			br.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
