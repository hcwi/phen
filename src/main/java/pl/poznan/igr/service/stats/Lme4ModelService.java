package pl.poznan.igr.service.stats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.analysis.Lme4ModelSession;
import pl.poznan.igr.service.stats.r.ScriptRunner;
import pl.poznan.igr.service.stats.r.ScriptStatus;

import java.io.File;

@Service
public class Lme4ModelService extends AbstractAnalysisService<Lme4ModelSession> {

    @Autowired
    private ScriptRunner scriptRunner;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Override
    protected TaskExecutor getAsyncExecutor() {
        return executor;
    }

    @Override
    protected Lme4ModelSession newSession() {
        return new Lme4ModelSession();
    }

    @Override
    protected Lme4ModelSession getSessionFromContext(Context ctx) {
        return ctx.getLme4ModelSession();
    }

    @Override
    protected void setSessionInContext(Context ctx, Lme4ModelSession session) {
        ctx.setLme4ModelSession(session);
    }

    @Override
    protected ScriptStatus runScript(String workingDirectory) {
        return scriptRunner.run("lme4model.R", new File(workingDirectory));
    }
}
