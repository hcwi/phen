package pl.poznan.igr.service.stats;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.analysis.AnalysisSession;
import pl.poznan.igr.domain.analysis.AnalysisStatus;
import pl.poznan.igr.service.stats.r.ScriptStatus;

import java.io.File;
import java.io.IOException;

public abstract class AbstractAnalysisService<T extends AnalysisSession> {

    private final static Logger log = LoggerFactory.getLogger(AbstractAnalysisService.class);

    protected boolean canProceed(Context ctx) {
        if (getSessionFromContext(ctx) == null) {
            return true;
        }

        if (getSessionFromContext(ctx).getStatus() == null) {
            return true;
        }

        if (getSessionFromContext(ctx).getStatus() == AnalysisStatus.ERROR) {
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void startAnalysis(Context ctx) {
        T session = getSessionFromContext(ctx);
        if (session == null) {
            session = newSession();
            session.setContext(ctx);
            setSessionInContext(ctx, session);
        }
        session.setStatus(AnalysisStatus.IN_PROGRESS);
    }

    @Transactional
    public void analyze(Context ctx) throws IOException {
        if (canProceed(ctx)) {
            log.debug("Starting analysis of " + ctx);
            startAnalysis(ctx);

            UnzipSession us = ctx.getUnzipSession();
            String path = us.getUnzipPath();
            ScriptStatus status = runScript(path);
            if (status.errorMessage.isPresent()) {
                log.debug("Error analysing " + ctx + ": " + status.errorMessage);
                getSessionFromContext(ctx).setStatus(AnalysisStatus.ERROR);
                getSessionFromContext(ctx).setMessage(status.errorMessage.get());
            } else {
                log.debug("Done analysing of " + ctx);
                getSessionFromContext(ctx).setStatus(AnalysisStatus.DONE);
                persistResults(ctx, path);
            }
        }
    }

    private void persistResults(Context ctx, String path) throws IOException {
        byte[] bytes = ctx.getImportSession().getBlobFile().getContent();
        // TODO: uncomment below to read the results
        // byte[] bytes = Files.toByteArray(new File(path + "/results.zip"));
        BlobFile results = new BlobFile("analyzed-" + ctx.getImportSession().getBlobFile().getFileName(), bytes);
        ctx.setResultFile(results);
    }

    protected abstract T newSession();

    protected abstract T getSessionFromContext(Context ctx);

    protected abstract void setSessionInContext(Context ctx, T session);

    protected abstract ScriptStatus runScript(String workingDirectory);
}
