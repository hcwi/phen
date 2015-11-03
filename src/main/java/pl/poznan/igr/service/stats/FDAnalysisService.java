package pl.poznan.igr.service.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.analysis.FDAnalysisSession;
import pl.poznan.igr.service.stats.r.ScriptRunner;
import pl.poznan.igr.service.stats.r.ScriptStatus;

import java.io.File;


@Service
public class FDAnalysisService extends AbstractAnalysisService<FDAnalysisSession> {

    private final static Logger log = LoggerFactory.getLogger(FDAnalysisService.class);

    @Autowired
    private ScriptRunner scriptRunner;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Override
    protected TaskExecutor getAsyncExecutor() {
        return executor;
    }

    @Override
    protected FDAnalysisSession newSession() {
        return new FDAnalysisSession();
    }

    @Override
    protected FDAnalysisSession getSessionFromContext(Context ctx) {
        return ctx.getFDAnalysisSession();
    }

    @Override
    protected void setSessionInContext(Context ctx, FDAnalysisSession session) {
        ctx.setFDAnalysisSession(session);
    }

    @Override
    protected ScriptStatus runScript(String workingDirectory) {
        return scriptRunner.run("fdAnalysis.R", new File(workingDirectory));
    }



}
