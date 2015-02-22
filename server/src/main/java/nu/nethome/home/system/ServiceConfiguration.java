package nu.nethome.home.system;

/**
 * Interface to access global configuration parameters of the service
 */
public interface ServiceConfiguration {

    /**
     * Get the file path to the directory where log and data files should be written.
     * This directory can be assumed writable by the process.
     *
     * @return full file path to the log directory including ending path separator
     */
    String getLogDirectory();
}
