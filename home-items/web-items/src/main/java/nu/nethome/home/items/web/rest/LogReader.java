/**
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.jfree.data.general.SeriesException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {

    private static final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String START_TIME_PARAMETER = "start";
    private static final int LINE_LENGTH=23;
    private static final String STOP_TIME_PARAMETER = "stop";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private HomeService service;

    public LogReader(HomeService server) {
        this.service = server;
    }

    public List<Object[]> getLog(String startTimeString, String stopTimeString, HomeItemProxy item) throws IOException {
        List<Object[]> result = new ArrayList<Object[]>();
        Date startTime = parseParameterDate(startTimeString);
        Date stopTime =  parseParameterDate(stopTimeString);

        if (stopTime == null) {
            stopTime = new Date();
        }
        if (startTime == null) {
            startTime = oneWeekBack(stopTime);
        }
        String fileName = null;
        if (item != null) {
            fileName = item.getAttributeValue("LogFile");
        }
        if (fileName != null) fileName = fromURL(fileName);

        try {
            // Open the data file
            FileReader reader = new FileReader(getFullFileName(fileName));
            Long startTimeMs = startTime.getTime();
            Long month = 1000L * 60L * 60L * 24L * 30L;
            boolean doOptimize = true;
            boolean justOptimized = false;
            boolean isFirst = true;
            BufferedReader br = new BufferedReader(reader);
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    try {
                        // Get next log entry
                        if (line.length() > 21) {
                            // Adapt the time format
                            String minuteTime = line.substring(0, 16).replace('.', '-');
                            // Parse the time stamp

                            Date min = fileDateFormat.parse(minuteTime);

                            // Ok, this is an ugly optimization. If the current time position in the file
                            // is more than two months (60 days) ahead of the start of the time window, we
                            // quick read 1 month worth of data, assuming that there is 4 samples per hour.
                            // This may lead to scanning past start of window if there are holes in the data
                            // series.
                            if (doOptimize && ((startTimeMs - min.getTime()) > month * 2)) {
                                long skiped = br.skip(24 * 4 * 30 * LINE_LENGTH);
                                justOptimized = true;
                                continue;
                            }
                            // Detect if we have scanned past the window start position just after an optimization scan.
                            // If this is the case it may be because of the optimization. In that case we have to switch
                            // optimization off and start over.
                            if ((min.getTime() > startTimeMs) && doOptimize && justOptimized) {
                                reader.reset();
                                doOptimize = false;
                                continue;
                            }
                            justOptimized = false;
                            // Check if value is within time window
                            if ((min.getTime() > startTimeMs) &&
                                    (min.getTime() < stopTime.getTime())) {
                                // Parse the value
                                double value = Double.parseDouble((line.substring(20)).replace(',', '.'));
                                // Add the entry
                                Object[] row = {dateFormat.format(min), value};
                                result.add(row);
                                isFirst = false;
                                doOptimize = false;
                            }
                        }
                    } catch (SeriesException se) {
                        // Bad entry, for example due to duplicates at daylight saving time switch
                    } catch (NumberFormatException nfe) {
                        // Bad number format in a line, try to continue
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                br.close();
            }
        } catch (FileNotFoundException f) {
            System.out.println(f.toString());
        }
        return result;
    }

    private String getFullFileName(String fileName) {
        if (fileName.contains(File.pathSeparator) || fileName.contains("/")) {
            return fileName;
        } else {
            return service.getConfiguration().getLogDirectory() + fileName;
        }
    }

    private void printEntry(ServletOutputStream p, Date time, double value, boolean isFirst) throws IOException {
        p.print(String.format("%s[\"%s\", %s]", (isFirst ? "" : ","), dateFormat.format(time), value));
    }

    private static Date oneWeekBack(Date stopTime) {
        return new Date(stopTime.getTime() - 1000L * 60L * 60L * 24L * 7L);
    }

    private static Date parseParameterDate(String timeString) {
        Date result = null;
        try {
            if (timeString != null) {
                result = inputDateFormat.parse(timeString);
            }
        } catch (ParseException e1) {
            // Silently ignore
        }
        return result;
    }

    public static String fromURL(String aURLFragment) {
        String result = null;
        try {
            result = URLDecoder.decode(aURLFragment, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }
}
