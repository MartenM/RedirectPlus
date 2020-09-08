package nl.martenm.redirectplus.objects;

/**
 * A simple wrapper for configuration errors.
 */
public class ConfigurationError {

    private String[] messages;
    private boolean fatal;

    /**
     * Will not be marked as fatal.
     * @param messages An array with error messages
     */
    public ConfigurationError(String[] messages) {
        this(messages, false);
    }

    /**
     * Can be marked as fatal
     * @param messages An array with error messages
     * @param fatal If the plugin should disable or not.
     */
    public ConfigurationError(String[] messages, boolean fatal) {
        this.messages = messages;
        this.fatal = fatal;
    }

    public String[] getMessages() {
        return messages;
    }

    /**
     * If the plugin should stop functioning in order to force the server owner to fix the issues.
     * @return
     */
    public boolean isFatal() {
        return fatal;
    }
}
