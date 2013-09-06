package pl.poznan.igr.service.stats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

public class RHandler extends Thread {

	InputStream is;
	String type;
	Logger log;

	public RHandler(InputStream is, String type, Logger log) {
		this.is = is;
		this.type = type;
		this.log = log;
	}

	public void run() {

		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;
			while ((line = br.readLine()) != null) {
				log.info(type + ": " + line);
			}
			br.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
