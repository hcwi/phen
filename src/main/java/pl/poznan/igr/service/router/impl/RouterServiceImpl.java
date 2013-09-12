package pl.poznan.igr.service.router.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.ServiceImpl;
import pl.poznan.igr.service.router.RouterService;
import pl.poznan.igr.service.stats.StatsService;
import pl.poznan.igr.service.unzip.UnzipService;
import pl.poznan.igr.service.zip.ZipService;

@Service
public class RouterServiceImpl extends ServiceImpl implements RouterService {

	@Autowired
	UnzipService unzipService;

	@Autowired
	StatsService statsService;

	@Autowired
	ZipService zipService;

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
		case ANALYSIS_SAVED:
			zipService.process(ctx);
			if (ctx.getStatusMessage().length() == 0) {
				ctx.setStatus(Status.FINISHED);
			} else {
				ctx.setStatus(Status.FINISHED_WITH_ERRORS);
			}
			ctx.setFinished(new Date());
			break;
		default:
			break;
		}
	}

}
