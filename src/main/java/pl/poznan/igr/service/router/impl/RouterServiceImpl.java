package pl.poznan.igr.service.router.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.router.RouterService;
import pl.poznan.igr.service.stats.StatsService;
import pl.poznan.igr.service.unzip.UnzipService;

@Service
public class RouterServiceImpl implements RouterService {

	@Autowired
	UnzipService unzipService;
	
	@Autowired
	StatsService statsService;
	
	
	@Override
	public void runNext(Context ctx) {
		
		Status status = ctx.getStatus();
		switch (status) {
		case UPLOADED:
			unzipService.process(ctx);
			break;
		case UNZIPPED:
			statsService.process(ctx);
			break;
		case ANALYSED_SAVED:
			ctx.setFinished(new Date());
			break;
		default:
			break;
		}

	}

}
