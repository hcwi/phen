package pl.poznan.igr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.analysis.AnalysisStatus;
import pl.poznan.igr.domain.analysis.Lme4ModelSession;
import pl.poznan.igr.service.stats.r.ScriptRunner;
import pl.poznan.igr.service.stats.r.ScriptStatus;

import java.io.File;

@Service
public class Lme4ModelService {

    private final static Logger log = LoggerFactory.getLogger(Lme4ModelService.class);


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
                ctx.getLme4ModelSession().setStatus(AnalysisStatus.ERROR);
                ctx.getLme4ModelSession().setMessage(status.errorMessage.get());
            } else {
                log.debug("Done analysing of " + ctx );
                ctx.getLme4ModelSession().setStatus(AnalysisStatus.DONE);
                ctx.setResultFile(ctx.getImportSession().getBlobFile()); // TODO: reupload new file
            }
        }
    }

    private boolean canProceed(Context ctx) {
        if (ctx.getLme4ModelSession() == null) {
            return true;
        }

        if (ctx.getLme4ModelSession().getStatus() == null) {
            return true;
        }

        if (ctx.getLme4ModelSession().getStatus() == AnalysisStatus.ERROR) {
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void startAnalysis(Context ctx) {
        Lme4ModelSession lme4ModelSession = ctx.getLme4ModelSession();
        if (lme4ModelSession == null) {
            lme4ModelSession = new Lme4ModelSession();
            lme4ModelSession.setContext(ctx);
            ctx.setLme4ModelSession(lme4ModelSession);
        }
        lme4ModelSession.setStatus(AnalysisStatus.IN_PROGRESS);
    }
}
