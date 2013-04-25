package pl.poznan.igr.service.stats.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.Context;
import pl.poznan.igr.service.stats.StatsService;

import org.rosuda.JRI.*;

@Service
public class StatsServiceImpl implements StatsService {

	@Override
	@Transactional
	public void calculateStats(Context ctx) {

		//TODO rethink using JRI
		//cannot be initialized twice
		//R_HOME, R.dll in path, jri.dll in path
		Rengine re = new Rengine (new String[]{"--no-save"}, false, null);
		System.out.println("Rengine created, waiting for R");
		
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
			return;
		}
		
		re.assign("a", new int[]{36});
	    REXP ans = re.eval("sqrt(a)");
	    Double result = ans.asDouble();
		System.err.println("\n\n R call result: " + result + "\n\n");
		
		//re.eval("sink('test_rengine.txt'); sqrt(25)");
		//re.run();
	    
		re.end();
	}

}
