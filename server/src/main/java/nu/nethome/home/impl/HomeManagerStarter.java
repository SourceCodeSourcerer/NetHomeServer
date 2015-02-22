/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.impl;

import nu.nethome.util.plugin.SelectivePluginScanner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * This is the boot strap loader of the server application. It reads command line parameters
 * and starts the server.
 *
 * @author Stefan
 */
public class HomeManagerStarter {
    private static final int NUMBER_OF_LOGFILES = 2;
    private static final int LOG_FILE_SIZE = 1000000;
    private static final String DEFAULT_XML = "default.xml";
    public static final String DEFAULT_PLUGIN_DIRECTORY = "../plugins";
    private Logger logger = Logger.getLogger(HomeManagerStarter.class.getName());


    public static void main(String[] args) {
        HomeManagerStarter me = new HomeManagerStarter();
        me.go(args);
    }

    protected String getDefaultFileName() {
        return DEFAULT_XML;
    }

    protected String getDefaultPluginDirectory() {
        return DEFAULT_PLUGIN_DIRECTORY;
    }

    public final void go(String[] args, HomeItemFactory... additionalFactories) {
        HomeServer server = new HomeServer();
        server.setName("Home Server");
        String logFileName = System.getProperty("user.home");

        // Check arguments - if no arguments, load the demo configuration
        int i = 0;
        List<String> pluginDirectories = new LinkedList<String>();
        pluginDirectories.add(getDefaultPluginDirectory());
        while (i < args.length && args[i].startsWith("-")) {
            if (args[i].startsWith("-p")) {
                pluginDirectories.add(args[i].substring(2));
            }
            if (args[i].startsWith("-l")) {
                logFileName = args[i].substring(2);
                /**
                 * For backwards compatibility we only set the log directory in the server if it is explicitly
                 * specified. Otherwise it would break functionality for older installations were all log files
                 * from thermometers and so on are stored in the current directory of the application.
                 */
                server.setLogDirectory(logFileName);
            }
            if (args[i].startsWith("-d") && args[i].length() > 2) {
                try {
                    int startupDelay = Integer.parseInt(args[i].substring(2)) * 1000;
                    Thread.sleep(startupDelay);
                } catch (NumberFormatException | InterruptedException e) {
                    // Ignore
                }
            }
            i++;
        }

        try {
            // Initialize logging ( http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/package-summary.html )
            logFileName += "/HomeManager%g.log";
            Handler fh = new FileHandler(logFileName, LOG_FILE_SIZE, NUMBER_OF_LOGFILES, true);
            fh.setFormatter(new LogFormatter());
            Logger.getLogger("").addHandler(fh);
            logger.info("**Starting HomeManager " + HomeManagerStarter.class.getPackage().getImplementationVersion() + "**");
            logger.info("Logging to: " + logFileName);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        // Create Plugin scanner and scan for plugins
        SelectivePluginScanner pluginProvider = new SelectivePluginScanner();
        try {
            List<File> files = new LinkedList<File>();
            for (String name : pluginDirectories) {
                files.add(new File(name));
            }
            pluginProvider.scanForPlugins(files);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not scan plugin directory", e);
        }

        HomeItemFactory pluginFactory = new PluginHomeItemFactory(pluginProvider);
        MultiHomeItemFactory factory = new MultiHomeItemFactory(pluginFactory);
        factory.addFactories(additionalFactories);
        HomeItemLoader loader = new HomeItemFileLoader();

        Preferences prefs = Preferences.userNodeForPackage(HomeManagerStarter.class);
        String fileName = prefs.get("SaveFileName", getDefaultFileName());
        // Check if a configuration file name is supplied
        if (i < args.length) {
            // Yes, a configuration file name was supplied, use that
            server.setFileName(args[i]);
        } else if (new File(fileName).exists()) {
            // Found a file name in system properties, or default file name
            server.setFileName(fileName);
        } else if (new File(getDefaultFileName()).exists()) {
            // Try default file name
            server.setFileName(getDefaultFileName());
        } else {
            // Fall back to the demo file supplied with the release
            server.setFileName("demo.xml");
        }
        server.run(factory, loader, pluginProvider);
    }
}
