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

package nu.nethome.home.items.web;

import nu.nethome.home.system.HomeService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * Generate a graph jpeg image given a file log of values.
 * @author Stefan Str�mberg
 */
public class GraphServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private final HomeService server;

    private SimpleDateFormat m_Format = new SimpleDateFormat("yyyyMMddHHmmss");

    public GraphServlet(HomeService server) {
        this.server = server;
    }

    /**
	 * This is the main enterence point of the class. This is called when a http request is
	 * routed to this servlet.
	 */
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		ServletOutputStream p = res.getOutputStream();
		Date startTime = null;
		Date stopTime = null;

		// Analyse arguments
		String fileName = req.getParameter("file");
		if (fileName != null) fileName = getFullFileName(fromURL(fileName));
		String startTimeString = req.getParameter("start");
		String stopTimeString = req.getParameter("stop");
		try {
			if (startTimeString != null) {
				startTime = m_Format.parse(startTimeString);
			}
			if (stopTimeString != null) {
				stopTime = m_Format.parse(stopTimeString);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String look = req.getParameter("look");
		if (look == null) look = "";

		TimeSeries timeSeries = new TimeSeries("Data", Minute.class);

		// Calculate time window
		Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY , 0);
		cal.set(Calendar.MINUTE , 0);
		cal.set(Calendar.SECOND , 0);
		Date startOfDay = cal.getTime();
		cal.set(Calendar.DAY_OF_WEEK , Calendar.MONDAY);
		Date startOfWeek = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH , 1);
		Date startOfMonth = cal.getTime();
		cal.set(Calendar.MONTH , Calendar.JANUARY);
		Date startOfYear = cal.getTime();  
		
		// if (startTime == null) startTime = startOfWeek;
		if (stopTime == null) stopTime = currentTime;
		if (startTime == null) startTime = new Date(stopTime.getTime() - 1000L * 60L * 60L * 24L * 2L);

		try {
			// Open the data file
			File logFile = new File(fileName);
			Scanner fileScanner = new Scanner(logFile);
			Long startTimeMs = startTime.getTime();
			Long month = 1000L * 60L * 60L * 24L * 30L;
			boolean doOptimize = true;
			boolean justOptimized = false;
			try {
				while (fileScanner.hasNext()) {
					try {
						// Get next log entry
						String line = fileScanner.nextLine();
						if (line.length() > 21) {
							// Adapt the time format
							String minuteTime = line.substring(0, 16).replace('.', '-');
							// Parse the time stamp
							Minute min = Minute.parseMinute(minuteTime);
							
							// Ok, this is an ugly optimization. If the current time position in the file
							// is more than a month (30 days) ahead of the start of the time window, we
							// quick read two weeks worth of data, assuming that there is 4 samples per hour.
							// This may lead to scanning past start of window if there are holes in the data
							// series.
							if (doOptimize && ((startTimeMs - min.getFirstMillisecond()) > month)) {
								for (int i = 0; (i < (24 * 4 * 14)) && fileScanner.hasNext(); i++) {
									fileScanner.nextLine();
								}
								justOptimized = true;
								continue;
							}
							// Detect if we have scanned past the window start position just after an optimization scan.
							// If this is the case it may be because of the optimization. In that case we have to switch 
							// optimization off and start over.
							if ((min.getFirstMillisecond() > startTimeMs) && doOptimize && justOptimized) {
								logFile = new File(fileName);
								fileScanner = new Scanner(logFile);
								doOptimize = false;
								continue;
							}
							justOptimized = false;
							// Check if value is within time window
							if ((min.getFirstMillisecond() > startTimeMs) &&
									(min.getFirstMillisecond() < stopTime.getTime())) {
								// Parse the value
								double value = Double.parseDouble((line.substring(20)).replace(',', '.'));
								// Add the entry
								timeSeries.add(min, value);
								doOptimize = false;
							}
						}
					}
					catch (SeriesException se) {
						// Bad entry, for example due to duplicates at daylight saving time switch
					}
					catch (NumberFormatException nfe) {
						// Bad number format in a line, try to continue
					}
				}
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
			finally {
				fileScanner.close();
			}
		}
		catch (FileNotFoundException f) {
			System.out.println(f.toString());
		}

		// Create a collection for plotting
		TimeSeriesCollection data = new TimeSeriesCollection();
		data.addSeries(timeSeries);

		JFreeChart chart;
		
		int xSize = 750;
		int ySize = 450;
		// Customize colors and look of the Graph.
		if (look.equals("mobtemp")) {
			// Look for the mobile GUI
			chart = ChartFactory.createTimeSeriesChart(null, null, null, data, false, false, false);
			XYPlot plot = chart.getXYPlot();
			ValueAxis timeAxis = plot.getDomainAxis();
			timeAxis.setAxisLineVisible(false);
			ValueAxis valueAxis = plot.getRangeAxis(0);
			valueAxis.setAxisLineVisible(false);
			xSize = 175;
			ySize = 180;			
		}
		else {
			// Create a Chart with time range as heading
			SimpleDateFormat localFormat = new SimpleDateFormat(); 
			String heading = localFormat.format(startTime) + " - " + localFormat.format(stopTime);
			chart = ChartFactory.createTimeSeriesChart(heading, null, null, data, false, false, false);

			Paint background = new Color(0x9D8140);
			chart.setBackgroundPaint(background);
			TextTitle title = chart.getTitle(); // fix title
			Font titleFont = title.getFont();
			titleFont = titleFont.deriveFont(Font.PLAIN, (float) 14.0);
			title.setFont(titleFont);
			title.setPaint(Color.darkGray);
			XYPlot plot = chart.getXYPlot();
			plot.setBackgroundPaint(background);
			plot.setDomainGridlinePaint(Color.darkGray);
			ValueAxis timeAxis = plot.getDomainAxis();
			timeAxis.setAxisLineVisible(false);
			ValueAxis valueAxis = plot.getRangeAxis(0);
			valueAxis.setAxisLineVisible(false);
			plot.setRangeGridlinePaint(Color.darkGray);
			XYItemRenderer renderer = plot.getRenderer(0);
			renderer.setSeriesPaint(0, Color.darkGray);
			xSize = 750;
			ySize = 450;
		}

		try
		{
			res.setContentType("image/png");
			res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "no-cache");
			res.setStatus( HttpServletResponse.SC_OK );
			ChartUtilities.writeChartAsPNG(p, chart, xSize, ySize);
		}
		catch (IOException e)
		{
			System.err.println("Problem occurred creating chart.");
		}

		p.flush();
		p.close();
		return;
	}

    private String getFullFileName(String fileName) {
        if (fileName.contains(File.pathSeparator) || fileName.contains("/")) {
            return fileName;
        } else {
            return server.getConfiguration().getLogDirectory() + fileName;
        }
    }

	public static String fromURL(String aURLFragment){
		String result = null;
		try {
			result = URLDecoder.decode(aURLFragment, "UTF-8");
		}
		catch (UnsupportedEncodingException ex){
			throw new RuntimeException("UTF-8 not supported", ex);
		}
		return result;
	}

}
