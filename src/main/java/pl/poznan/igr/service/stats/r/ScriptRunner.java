package pl.poznan.igr.service.stats.r;

import java.io.File;

public interface ScriptRunner {

    ScriptStatus run(String script, File workingDirectory);

}
