package pl.poznan.igr.service.stats;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractAnalysisService<T extends AnalysisSession> {

    private final static Logger log = LoggerFactory.getLogger(AbstractAnalysisService.class);

    public void analyze(final Context ctx) throws IOException {
        if (canProceed(ctx)) {
            log.debug("Starting analysis of " + ctx);
            startAnalysis(ctx);
            getAsyncExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    runAnalysis(ctx);
                }
            });
        }
    }

    @Transactional
    private boolean canProceed(Context ctx) {
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

    @Transactional
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
    private void runAnalysis(Context ctx) {
        try {
            UnzipSession us = ctx.getUnzipSession();
            String path = us.getUnzipPath();
            ScriptStatus status = runScript(path);
            if (status.errorMessage.isPresent()) {
                persistFailure(ctx, status);
            } else {
                log.debug("Done analysing of " + ctx);
                persistSuccessResults(ctx, path);
            }
        } catch (Exception e) {
            log.error("Error while analyzing", e);
            if (getSessionFromContext(ctx).getStatus() != AnalysisStatus.ERROR) {
                getSessionFromContext(ctx).setMessage("Error while analyzing, please contact support");
                getSessionFromContext(ctx).setStatus(AnalysisStatus.ERROR);
                ctx.merge();
            }
        }
    }

    private void persistFailure(Context ctx, ScriptStatus status) {
        log.debug("Error analysing " + ctx + ": " + status.errorMessage);
        getSessionFromContext(ctx).setStatus(AnalysisStatus.ERROR);
        getSessionFromContext(ctx).setMessage(status.errorMessage.get());
        ctx.merge();
    }

    private void persistSuccessResults(Context ctx, String path) {
        try {
            byte[] bytes = Files.toByteArray(new File(path + File.separator + "results.zip"));
            BlobFile results = new BlobFile("analyzed-" + ctx.getImportSession().getBlobFile().getFileName(), bytes);
            ctx.setResultFile(results);
            getSessionFromContext(ctx).setStatus(AnalysisStatus.DONE);
        } catch (IOException e) {
            log.error("Can't save results for " + ctx, e);
            getSessionFromContext(ctx).setStatus(AnalysisStatus.ERROR);
            getSessionFromContext(ctx).setMessage("Can't save results, please contact support");
        } finally {
            ctx.merge();
        }
    }

    protected abstract T newSession();

    protected abstract T getSessionFromContext(Context ctx);

    protected abstract void setSessionInContext(Context ctx, T session);

    protected abstract ScriptStatus runScript(String workingDirectory);

    protected abstract TaskExecutor getAsyncExecutor();
}
