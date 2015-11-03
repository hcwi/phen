package pl.poznan.igr.service.stats.r;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class RScriptRunner implements ScriptRunner {

    private final static Logger log = LoggerFactory.getLogger(RScriptRunner.class);

    private final static Joiner NEW_LINE_JOINER = Joiner.on("\n");

    @Value("#{environment.R_SCRIPT}")
    private String rScriptRunner;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(rScriptRunner)) {
            rScriptRunner = "/usr/bin/Rscript";
        }
    }

    @Override
    public ScriptStatus run(String scriptFileName, File workingDirectory) {
        ScriptStatus result;
        try {
            String scriptPath = getScriptPath(scriptFileName);

            String[] args = new String[]{rScriptRunner, scriptPath, workingDirectory.getAbsolutePath()};
            log.info("Command {} {} {}", args);
            Process p = Runtime.getRuntime().exec(args);

            ProcessOutputReaderThread stdout = new ProcessOutputReaderThread(p.getInputStream(), "INPUT", log);
            ProcessOutputReaderThread stderr = new ProcessOutputReaderThread(p.getErrorStream(), "ERROR", log);
            stdout.start();
            stderr.start();

            List<String> errors = new ArrayList<>();

            waitForErrorMessagesIfAny(p, errors);
            waitForErrorMessagesIfAny(stdout, errors);
            waitForErrorMessagesIfAny(stderr, errors);

            result = errors.size() == 0 ?
                    ScriptStatus.ok() :
                    ScriptStatus.error(NEW_LINE_JOINER.join(errors));

        } catch (RException | IOException | InterruptedException e ) {
            log.error("R communication error", e);
            result = ScriptStatus.error("Internal Server Error, Please contact support");
        }
        return result;
    }


    private String getScriptPath(String scriptFileName) {
        URL scriptUrl = this.getClass().getClassLoader()
                .getResource(scriptFileName);
        if (scriptUrl == null) {
            throw new RException("Couldn't find script at "
                    + scriptFileName + ". Analysis failed.");
        }

        // TODO: get rid of ugly replace
        String scriptPath = scriptUrl.getPath();
        if (System.getProperty("os.name").startsWith("Windows")) {
            scriptPath = scriptPath.substring(1).replace("/", "\\");
        }

        log.debug("script = {}", scriptPath);

        return scriptPath;
    }

    private void waitForErrorMessagesIfAny(Process p, List<String> errors) throws InterruptedException {
        int success = p.waitFor();
        if (success != 0) {
            log.error("Process exited with error code {}", success);
            errors.add("Analysis of the dataset failed. Probably there are errors in the processed ISA-TAB files.");
        }
    }

    private void waitForErrorMessagesIfAny(ProcessOutputReaderThread processOutputReaderThread, List<String> errors) throws InterruptedException {
        processOutputReaderThread.join();
        Optional<String> errorMessage = processOutputReaderThread.getErrorMessage();
        if (errorMessage.isPresent()) {
            errors.add(errorMessage.get());
        }
    }
}
