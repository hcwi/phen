package pl.poznan.igr.service.stats.r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import pl.poznan.igr.domain.Context;

public class ProcessOutputReaderThread extends Thread {

	private final InputStream is;
	private final String type;
	private final Logger log;
	private final StringBuilder buf;

	public ProcessOutputReaderThread(InputStream is, String type, Logger log) {
		this.is = is;
		this.type = type;
		this.log = log;
		this.buf = new StringBuilder();
	}

    public Optional<String> getErrorMessage() {
        String raw = buf.toString();
        if (StringUtils.isEmpty(raw)) {
            return Optional.empty();
        } else {
            return Optional.of(raw);
        }
    }

	public void run() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("ERROR")) {
                    buf.append(line + "\n");
                    log.error(type + ": " + line);
                }
                else {
                    log.debug(type + ": " + line);
                }
            }
        } catch (IOException e) {
            log.error("Unexpected error while reading the process output", e);
        }
	}
}
