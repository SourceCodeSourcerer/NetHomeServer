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

package nu.nethome.home.item;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * This is a component for adding file logging capabilities to a Value-item, for example
 * a thermometer. It will automatically sample values from the item specified in
 * the constructor and store them in the log file.
 * To add this component, add the following lines to a Value-Item:<br>
 * In Model: <br>
 * <pre> + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"</pre>
 * As attribute:<br>
 * <pre> protected LoggerComponent m_TempLogger = new LoggerComponent(this);</pre>
 * In Activate:<br>
 * <pre> m_TempLogger.activate();</pre>
 * In stop:<br>
 * <pre> m_TempLogger.stop();</pre>
 * For access:<br>
 * <pre>public String getLogFile() {
 * return m_TempLogger.getFileName();
 * }
 * public void setLogFile(String LogFile) {
 * m_TempLogger.setFileName(LogFile);
 * }</pre>
 *
 * @author Stefan Strömberg
 */
@SuppressWarnings("UnusedDeclaration")
public class LoggerComponent extends TimerTask {

    private Timer logTimer = new Timer("Logger Component", true);
    private static Logger logger = Logger.getLogger(LoggerComponent.class.getName());
    private boolean loggerIsActivated = false;
    private boolean loggerIsRunning = false;
    private String logDirectoryPath = "";
    // Public attributes
    private String logFileName = "";
    private int logInterval = 15;
    private String logTimeFormat = "yyyy.MM.dd HH:mm:ss;";
    private ValueItem loggedItem = null;

    public LoggerComponent(ValueItem logged) {
        loggedItem = logged;
    }

    public void activate() {
        loggerIsActivated = true;
        if (logFileName.length() == 0) {
            return;
        }
        // Get current time
        Calendar date = Calendar.getInstance();
        // Start at next even hour
        date.set(Calendar.HOUR, date.get(Calendar.HOUR) + 1);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        // Schedule the job at m_Interval minutes interval
        logTimer.schedule(
                this,
                date.getTime(),
                1000L * 60 * logInterval
        );
        loggerIsRunning = true;
    }

    protected void activate(String logDirectoryPath) {
        this.logDirectoryPath = logDirectoryPath;
        activate();
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        logTimer.cancel();
        loggerIsRunning = false;
        loggerIsActivated = false;
    }

    public void run() {
        logger.fine("Value Log Timer Fired");
        BufferedWriter out = null;
        try {
            String value = loggedItem.getValue();
            if (value.length() > 0) {
                out = new BufferedWriter(new FileWriter(getFullFileName(), true));
                // Format the current time.
                SimpleDateFormat formatter
                        = new SimpleDateFormat(logTimeFormat);
                Date currentTime = new Date();
                String newLogLine = formatter.format(currentTime) + value;
                out.write(newLogLine);
                out.newLine();
            }
        } catch (IOException e) {
            logger.warning("Failed to open log file: " + getFullFileName() + " Error:" + e.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private String getFullFileName() {
        if (logFileName.contains(File.pathSeparator) || logFileName.contains("/")) {
            return logFileName;
        } else {
            return logDirectoryPath + logFileName;
        }
    }

    /**
     * @return Returns the FileName.
     */
    public String getFileName() {
        return logFileName;
    }

    /**
     * @param fileName The FileName to set.
     */
    public void setFileName(String fileName) {
        logFileName = fileName;
        // If we got a file name, and we are activated but not running - then start
        if ((fileName.length() != 0) && loggerIsActivated && !loggerIsRunning) {
            activate();
        }
    }

    /**
     * @return Returns the Interval.
     */
    public String getInterval() {
        return Integer.toString(logInterval);
    }

    /**
     * @param interval The Interval to set.
     */
    public void setInterval(String interval) {
        logInterval = Integer.parseInt(interval);
    }

    /**
     * @return Returns the TimeFormat.
     */
    public String getTimeFormat() {
        return logTimeFormat;
    }

    /**
     * @param timeFormat The TimeFormat to set.
     */
    public void setTimeFormat(String timeFormat) {
        logTimeFormat = timeFormat;
    }

    /**
     * @return the IsActivated
     */
    public boolean isActivated() {
        return loggerIsActivated;
    }
}
