package nu.nethome.home.item;

import nu.nethome.home.system.HomeService;

/**
 *
 */
public class ExtendedLoggerComponent extends LoggerComponent {
    public ExtendedLoggerComponent(ValueItem logged) {
        super(logged);
    }

    public void activate(HomeService server) {
        String logPath = "";
        if (server != null) {
            logPath = server.getConfiguration().getLogDirectory();
        }
        super.activate(logPath);
    }
}
