package pl.poznan.igr.service.stats.r;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScriptStatus {

    public final Optional<String> errorMessage;

    public ScriptStatus(Optional<String> errorMessage) {
        this.errorMessage = checkNotNull(errorMessage);
    }

    public static ScriptStatus ok() {
        return new ScriptStatus(Optional.<String>empty());
    }

    public static ScriptStatus error(String message) {
        return new ScriptStatus(Optional.of(message));
    }
}
