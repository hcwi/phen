package pl.poznan.igr.service.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.analysis.AnalysisStatus;
import pl.poznan.igr.domain.analysis.SufficientStatisticsSession;
import pl.poznan.igr.service.stats.r.ScriptRunner;
import pl.poznan.igr.service.stats.r.ScriptStatus;

import java.io.File;

@Service
public class SufficientStatisticsService {

    private final static Logger log = LoggerFactory.getLogger(SufficientStatisticsService.class);


    @Autowired
    private ScriptRunner scriptRunner;

    @Transactional
    public void analyze(Context ctx) {
        if (canProceed(ctx)) {
            log.debug("Starting analysis of " + ctx );
            startAnalysis(ctx);

            UnzipSession us = ctx.getUnzipSession();
            String path = us.getUnzipPath();
            ScriptStatus status = scriptRunner.run("SufficientStatistics", new File(path));
            if (status.errorMessage.isPresent()) {
                log.debug("Error analysing " + ctx + ": " + status.errorMessage);
                ctx.getSufficientStatisticsSession().setStatus(AnalysisStatus.ERROR);
                ctx.getSufficientStatisticsSession().setMessage(status.errorMessage.get());
            } else {
                log.debug("Done analysing of " + ctx );
                ctx.getSufficientStatisticsSession().setStatus(AnalysisStatus.DONE);
                ctx.setResultFile(ctx.getImportSession().getBlobFile()); // TODO: reupload new file
            }
        }
    }

    private boolean canProceed(Context ctx) {
        if (ctx.getSufficientStatisticsSession() == null) {
            return true;
        }

        if (ctx.getSufficientStatisticsSession().getStatus() == null) {
            return true;
        }

        if (ctx.getSufficientStatisticsSession().getStatus() == AnalysisStatus.ERROR) {
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void startAnalysis(Context ctx) {
        SufficientStatisticsSession sufficientStatisticsSession = ctx.getSufficientStatisticsSession();
        if (sufficientStatisticsSession == null) {
            sufficientStatisticsSession = new SufficientStatisticsSession();
            sufficientStatisticsSession.setContext(ctx);
            ctx.setSufficientStatisticsSession(sufficientStatisticsSession);
        }
        sufficientStatisticsSession.setStatus(AnalysisStatus.IN_PROGRESS);
    }

}
