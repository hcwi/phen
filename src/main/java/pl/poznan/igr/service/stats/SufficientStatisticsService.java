package pl.poznan.igr.service.stats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.analysis.SufficientStatisticsSession;
import pl.poznan.igr.service.stats.r.ScriptRunner;
import pl.poznan.igr.service.stats.r.ScriptStatus;

import java.io.File;

@Service
public class SufficientStatisticsService extends AbstractAnalysisService<SufficientStatisticsSession> {

    @Autowired
    private ScriptRunner scriptRunner;

    @Override
    protected SufficientStatisticsSession newSession() {
        return new SufficientStatisticsSession();
    }

    @Override
    protected SufficientStatisticsSession getSessionFromContext(Context ctx) {
        return ctx.getSufficientStatisticsSession();
    }

    @Override
    protected void setSessionInContext(Context ctx, SufficientStatisticsSession session) {
        ctx.setSufficientStatisticsSession(session);
    }

    @Override
    protected ScriptStatus runScript(String workingDirectory) {
        return scriptRunner.run("sufficientStatistics.R", new File(workingDirectory));
    }
}
