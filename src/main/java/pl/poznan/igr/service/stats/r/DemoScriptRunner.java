package pl.poznan.igr.service.stats.r;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DemoScriptRunner implements ScriptRunner {

    @Override
    public ScriptStatus run(String script, File workingDirectory) {
        return ScriptStatus.ok();
    }
}
