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

import nu.nethome.home.item.*;
import nu.nethome.home.system.*;
import nu.nethome.util.plugin.PluginProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;


/**
 * This is the main class of the NetHomeServer. It will start and manage all other HomeItem instances.
 *
 * @author Stefan Stromberg
 */
@SuppressWarnings("UnusedDeclaration")
public class HomeServer implements HomeItem, HomeService, ServiceState, ServiceConfiguration, ValueItem {

    private static final String MODEL;

    static {
        MODEL = ("<?xml version = \"1.0\"?> \n"
                + "<HomeItem Class=\"HomeServer\"  Category=\"Ports\">"
                + "  <Attribute Name=\"Version\" Type=\"String\" Get=\"getVersion\" Default=\"true\" />"
                + "  <Attribute Name=\"SentEventCount\" Type=\"String\" Get=\"getSentEventCount\" />"
                + "  <Attribute Name=\"EventsPerMinute\" Type=\"String\" Get=\"getEventsPerMinute\" />"
                + "  <Attribute Name=\"FileName\" Type=\"String\" Get=\"getFileName\" Set=\"setFileName\" />"
                + "  <Attribute Name=\"UpgradeCommand\" Type=\"String\" Get=\"getUpgradeCommand\" Set=\"setUpgradeCommand\" />"
                + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
                + "  <Attribute Name=\"UpTime\" Type=\"String\" Get=\"getUpTime\" />"
                + "  <Attribute Name=\"MaxDistributionTime\" Type=\"String\" Get=\"getMaxDistributionTime\" Unit=\"ms\" />"
                + "  <Attribute Name=\"AverageDistributionTime\" Type=\"String\" Get=\"getAverageDistributionTime\"  Unit=\"ms\" />"
                + "  <Attribute Name=\"MaxItemTime\" Type=\"String\" Get=\"getMaxItemTime\"  Unit=\"ms\" />"
                + "  <Attribute Name=\"MaxItemName\" Type=\"String\" Get=\"getMaxItemName\" />"
                + "  <Attribute Name=\"AlarmCount\" Type=\"String\" Get=\"getCurrentAlarmCountString\" />"
                + "  <Attribute Name=\"TotalLogRows\" Type=\"String\" Get=\"getTotalLogRecordCountString\" />"
                + "  <Action Name=\"LoadItems\" Method=\"loadItems\" />"
                + "  <Action Name=\"SaveItems\" Method=\"saveItems\" />"
                + "  <Action Name=\"StopServer\" Method=\"stopServer\" />"
                + "  <Action Name=\"UpgradeServer\" Method=\"upgradeServer\" />"
                + "  <Action Name=\"ResetStatistics\" Method=\"resetStatistics\" />"
                + "  <Action Name=\"ClearLog\" Method=\"clearLog\" />"
                + "</HomeItem> ");
    }

    private static final int MAX_QUEUE_SIZE = 20;
    private static final String QUIT_EVENT = "BrokerQuitEvent";
    public static final int LOG_RECORD_CAPACITY = 50;
    public static final int EVENT_COUNT_PERIOD = 15;

    private static Logger logger = Logger.getLogger(HomeServer.class.getName());
    private static final int MS_PER_MINUTE = (1000 * 60);
    private static final int MS_PER_HOUR = (MS_PER_MINUTE * 60);
    private static final int MS_PER_DAY = (MS_PER_HOUR * 24);
    private static final int UPGRADE_HOLDOFF_TIME = 500;
    private static final int MINUTES_PER_HOUR = 60;
    private String name;
    private long id = 1L;
    private boolean doUpgrade = false;
    private String upgradeCommand = "/usr/local/lib/home-manager/upgrade.sh";
    private Date startTime = new Date();
    private long maxID = 0;
    private LinkedBlockingQueue<Event> eventQueue;
    private EventDistributionStatistics statistics = new EventDistributionStatistics();
    private Timer minuteTimer = new Timer();
    private Event minuteEvent;
    private String fileName = "system.xml";
    private int sentEventCount = 0;
    private final ItemDirectory itemDirectory = new ItemDirectory();
    private HomeItemFactory factory;
    private HomeItemLoader homeItemLoader;
    private PluginProvider pluginProvider;
    private LinkedBlockingDeque<LogRecord> logRecords;
    private long totalLogRecordCount = 0;
    private int currentWarningCount = 0;
    private boolean activated = false;
    private List<FinalEventListener> finalEventListeners = new LinkedList<FinalEventListener>();
    private ExtendedLoggerComponent eventCountlogger = new ExtendedLoggerComponent(this);
    private long eventsCount = 0;
    private long eventsCountPerPeriod = 0;
    private int minuteCounter;
    private int minutesBetweenItemSave = 60;
    private String logDirectory = "";

    public HomeServer() {
        eventQueue = new LinkedBlockingQueue<Event>(MAX_QUEUE_SIZE);
        logRecords = new LinkedBlockingDeque<LogRecord>(LOG_RECORD_CAPACITY);
        setupLogger();
        eventCountlogger.activate(this);
    }

    private void setupLogger() {
        Logger.getLogger("").addHandler(new Handler() {
            @Override
            synchronized public void publish(LogRecord record) {
                newLogRecord(record);
            }

            @Override
            public void flush() {
                //Nothing to do
            }

            @Override
            public void close() throws SecurityException {
                logRecords.clear();
            }
        });
    }

    private void newLogRecord(LogRecord record) {
        if (isLogRecordInBlacklist(record)) {
            return;
        }
        totalLogRecordCount++;
        if (logRecords.remainingCapacity() == 0) {
            if (logRecords.getLast().getLevel().intValue() >= Level.WARNING.intValue()) {
                currentWarningCount--;
            }
            logRecords.removeLast();
        }

        logRecords.offerFirst(record);
        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            currentWarningCount++;
            if (record.getLevel().intValue() < Level.SEVERE.intValue()) {
                // Severe errors are not safe to send as Events, as the system cannot be trusted and we may end up
                // with recursive calls since it may be the event mechanism that is failing.
                Event alarmEvent = new InternalEvent("Alarm", record.getMessage());
                send(alarmEvent);
            }
        }
    }

    private boolean isLogRecordInBlacklist(LogRecord record) {
        return record.getMessage().startsWith("Prefs file removed in background") ||
            record.getMessage().startsWith("Could not open/create prefs root node");
    }

    public String clearLog() {
        logRecords.clear();
        currentWarningCount = 0;
        return "";
    }

    public void run(HomeItemFactory factory, HomeItemLoader loader, PluginProvider pluginProvider) {
        this.factory = factory;
        this.homeItemLoader = loader;
        this.pluginProvider = pluginProvider;

        // Load the configuration file
        loadItems();

        waitForEnd();

        // When we get this far, the application is closing down.
        // Stop all HomeItems and empty the instance list.
        stopAndRemoveItems();

        // Upgrade server
        handleUpgrade();
        logger.info("**Exiting HomeManager " + HomeManager.class.getPackage().getImplementationVersion() + "**");
    }

    private synchronized void waitForEnd() {
        try {
            wait();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted waiting for end of program", e);
        }
    }

    public void stopServer() {
        saveItems();
        internalStopServer();
    }

    private synchronized void internalStopServer() {
        notify();
    }

    public HomeService getService() {
        return this;
    }

    public ServiceConfiguration getConfiguration() {
        return this;
    }

    public void activate(HomeService server) {
        Thread eventThread = new Thread("EventDistributor") {
            @Override
            public void run() {
                eventDistributorTask();
            }
        };
        eventThread.start();
        minuteEvent = new InternalEvent(MINUTE_EVENT_TYPE);
        Calendar date = Calendar.getInstance();
        // Start at next next even minute
        boolean hourJump = (date.get(Calendar.MINUTE) == (MINUTES_PER_HOUR - 1));
        date.set(Calendar.HOUR, date.get(Calendar.HOUR) + (hourJump ? 1 : 0));
        date.set(Calendar.MINUTE, (hourJump ? 1 : (date.get(Calendar.MINUTE) + 2)));
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        // Schedule the job at m_Interval minutes interval
        minuteTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        send(minuteEvent);
                    }
                },
                date.getTime(),
                MS_PER_MINUTE
        );
        activated = true;
    }

    /*
      * (non-Javadoc)
      *
      * @see ssg.home.HomeItem#setName()
      */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        if (activated && !fileName.equals(this.fileName)) {
            // Kind of ugly. If the user changes the save file name, we also save this as a system property
            // so we can find the name (file) again when the server is started.
            Preferences prefs = Preferences.userNodeForPackage(HomeManager.class);
            prefs.put("SaveFileName", fileName);
        }
        this.fileName = fileName;
    }

    public List<DirectoryEntry> listInstances(String pattern) {
        return itemDirectory.listInstances(pattern);
    }

    public HomeItemProxy openInstance(String name) {
        HomeItem item = itemDirectory.findInstance(name);
        if (item != null) {
            try {
                return new LocalHomeItemProxy(item, this);
            } catch (ModelException e) {
                // return null
            }
        }
        return null;
    }

    public boolean renameInstance(String fromInstanceName, String toInstanceName) {
        return itemDirectory.renameInstance(fromInstanceName, toInstanceName);
    }

    public boolean removeInstance(String instanceName) {
        HomeItem item = itemDirectory.removeInstance(instanceName);
        if (item == null) {
            // Item does not exist
            return false;
        }

        // Stop the instance unless it is never started
        if (!item.getName().startsWith("#")) {
            item.stop();
        }
        return true;
    }

    public Event createEvent(String type, String value) {
        return new InternalEvent(type, value);
    }

    /*
      * (non-Javadoc)
      *
      * @see ssg.home.EventBroker#send(ssg.home.Event)
      *
      * This implementation iterates all HomeItems connected to the EventBroker
      * and sends the event to each of them.
      */
    public void send(Event event) {
        logger.fine(event.toString());
        if (!eventQueue.offer(event)) {
            handleEventDistributionFaliure(event);
        } else {
            sentEventCount++;
            eventsCount++;
        }
    }

    private void handleEventDistributionFaliure(Event event) {
        if (statistics.isItemCurrentlyProcessingEvent()) {
            logger.severe("Event queue full. Current Item processing is \"" + statistics.getCurrentItemName() + "\"  since " + getCurrentItemProcessingTime() + " ms");
        } else {
            logger.severe("Event queue full trying to distribute " + event.toString());
        }
    }

    public void eventDistributorTask() {
        while (true) {
            Event event;
            String itemName = "";
            try {
                // Take the next event from the queue, will wait if no events yet
                event = eventQueue.take();
                // Check if it was the quit event, quit in that case
                if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(QUIT_EVENT)) {
                    return;
                }
                // Loop over all Items and offer the event
                statistics.startDistributionRound();
                boolean eventIsHandled = false;
                for (HomeItem home : itemDirectory.getHomeItems()) {
                    try {
                        itemName = home.getName();
                        logger.finest("Distributing event to " + itemName);
                        statistics.startItemDistribution(itemName);
                        eventIsHandled |= home.receiveEvent(event);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to distribute event to \"" + itemName + "\" (" + event.toString() + ") ", e);
                    }
                    statistics.endItemDistribution();
                }
                synchronized (finalEventListeners) {
                    for (FinalEventListener listener : finalEventListeners) {
                        listener.receiveFinalEvent(event, eventIsHandled);
                    }
                }
            } catch (InterruptedException e) {
                // Do Dinada
            } finally {
                statistics.endDistributionRound();
            }
        }
    }

    /**
     * Register a new HomeItem
     *
     * @param item Instance to register
     */
    public int registerInstance(HomeItem item) {
        return itemDirectory.registerInstance(item);
    }

    public String getModel() {
        return MODEL;
    }

    public boolean receiveEvent(Event e) {
        if (e.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(MINUTE_EVENT_TYPE) && ++minuteCounter >= minutesBetweenItemSave) {
            saveItems();
            minuteCounter = 0;
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public long getItemId() {
        return id;
    }

    public void setItemId(long id) {
        this.id = id;
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        // Stop the event distribution thread by sending the quit event
        Event quitEvent = new InternalEvent(QUIT_EVENT);
        send(quitEvent);

        // Stop the minute timer
        minuteTimer.cancel();
    }

    public String getSentEventCount() {
        return String.valueOf(sentEventCount);
    }

    /**
     * Load HomeItems from a file and activate them. This is done in two steps: First the Items are
     * registered in the internal directory. After that the Items are activated in start order.
     * This means that at activation, all Items are reachable in the directory even those which are
     * not yet activated.
     */
    public void loadItems() {
        String currentFileName = getFileName();
        List<HomeItem> sortedItems = homeItemLoader.loadItems(getFileName(), factory, this);

        for (HomeItem item : sortedItems) {
            if (item.getItemId() > maxID) {
                maxID = item.getItemId();
            }
        }
        maxID += 1;

        // Loop through all created Items, and register them
        for (HomeItem item : sortedItems) {

            // This is a backward compatibility check. If the Item has no valid ID, assign one
            if (item.getItemId() == 0) {
                item.setItemId(maxID);
                maxID += 1;
            }

            // Register the new instance
            int regResult = registerInstance(item);
            if (regResult != 0) {
                // IF we fail to register the instance, mark it as bad by setting ID = 0
                HomeServer.logger.warning("Failed to register Item " + item.getName() + " Error " + Integer.toString(regResult));
                item.setItemId(0);
            }
        }

        // Now loop through all Items again in start order and activate them
        int itemCount = sortedItems.size();
        int activatedItemCount = 0;
        for (HomeItem item : sortedItems) {
            if (!item.getName().startsWith("#") && (item.getItemId() != 0)) {
                try {
                    item.activate(this);
                    activatedItemCount++;
                } catch (Exception e) {
                    HomeServer.logger.warning("Failed to activate Item " + item.getName() + " Error " + e.getMessage());
                }
            }
        }
        HomeServer.logger.info("Activated " + Integer.toString(activatedItemCount) + " of " + Integer.toString(itemCount) + " Items");
        setFileName(currentFileName);
    }

    public void saveItems() {
        List<HomeItem> items = new LinkedList<HomeItem>();
        Iterator<HomeItem> i = itemDirectory.iterator();
        while (i.hasNext()) {
            items.add(i.next());
        }
        homeItemLoader.saveItems(items, fileName);
    }

    /**
     * Stop all HomeItems and empty the internal instance lists
     */
    public void stopAndRemoveItems() {
        Iterator<HomeItem> it = itemDirectory.iterator();
        logger.info("Closing down");
        while (it.hasNext()) {
            try {
                HomeItem item = it.next();
                logger.info("Stopping: " + item.getName());
                item.stop();
            } catch (Exception e) {
                logger.warning("Exception caught during stop of Item: " + e.getMessage());
            }
        }
        logger.info("Stopped Items");
        itemDirectory.clear();
        eventCountlogger.stop();
        logger.info("Stopped Internal services");
    }

    /**
     * Initiate the upgrade sequence
     */
    public void upgradeServer() {
        doUpgrade = true;
        stopServer();
    }

    public void handleUpgrade() {
        if (doUpgrade) {
            try {
                logger.info("Starting upgrade sequence");
                Runtime r = Runtime.getRuntime();
                // Run the upgrade command. If this is a Windows bat-file, you have to have one bat file which
                // does a "start" of the second real upgrade bat file.
                r.exec(upgradeCommand);
                try {
                    // For some reason we have to wait a while, otherwise it seems this program exits before
                    // the execution of the upgrade command is really started.
                    Thread.sleep(UPGRADE_HOLDOFF_TIME);
                } catch (InterruptedException i) {
                    // Do nothing
                }
            } catch (IOException e) {
                logger.warning("Could not auto upgrade:" + e.getMessage());
            }
        }
    }

    /**
     * @return the upgradeCommand
     */
    public String getUpgradeCommand() {
        return upgradeCommand;
    }

    /**
     * @param upgradeCommand the upgradeCommand to set
     */
    public void setUpgradeCommand(String upgradeCommand) {
        this.upgradeCommand = upgradeCommand;
    }

    public String getUpTime() {
        long currentTime = new Date().getTime();
        long uptime = currentTime - startTime.getTime();
        long days = uptime / MS_PER_DAY;
        uptime = uptime - days * MS_PER_DAY;
        long hours = uptime / MS_PER_HOUR;
        uptime = uptime - hours * MS_PER_HOUR;
        long minutes = uptime / MS_PER_MINUTE;
        String result = (days != 0) ? Long.toString(days) + " days " : "";
        result += (hours != 0) ? Long.toString(hours) + " hours " : "";
        return result + Long.toString(minutes) + " minutes";
    }

    public HomeItemProxy createInstance(String publicClassName, String instanceName) {
        // Check name
        if (itemDirectory.findInstance(instanceName) != null) {
            return null;
        }

        // Try to create an instance of the new class
        HomeItem newItem = factory.createInstance(publicClassName);
        if (newItem == null) {
            return null;
        }
        // Set the name, ID and register the new instance
        try {
            newItem.setName(instanceName);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed setting name on HomeItem: " + instanceName, e);
        }
        maxID += 1;
        newItem.setItemId(maxID);
        registerInstance(newItem);
        try {
            return new LocalHomeItemProxy(newItem, this);
        } catch (ModelException e) {
            return null;
        }
    }

    public List<HomeItemInfo> listClasses() {
        return factory.listItemTypes();
    }

    public PluginProvider getPluginProvider() {
        return pluginProvider;
    }

    public ServiceState getState() {
        return this;
    }

    public String getMaxDistributionTime() {
        Double value = statistics.getMaxRoundTime();
        return String.format("%.2f", value);
    }

    public String getAverageDistributionTime() {
        Double value = statistics.getAvarageRoundTime();
        return String.format("%.2f", value);
    }

    public String getMaxItemTime() {
        Double value = statistics.getMaxItemTime();
        return String.format("%.2f", value);
    }

    public String getCurrentItemProcessingTime() {
        Double value = statistics.currentItemProcessingTime() / EventDistributionStatistics.NANO_PER_MILLI;
        return String.format("%.2f", value);
    }

    public String getMaxItemName() {
        return statistics.getMaxItemName();
    }

    public void resetStatistics() {
        statistics.resetStatistics();
    }

    public String getVersion() {
        String version = HomeManager.class.getPackage().getImplementationVersion();
        return (version == null) ? "Unknown" : version;
    }

    public Collection<LogRecord> getCurrentLogRecords() {
        return new ArrayList<LogRecord>(logRecords);
    }

    public int getCurrentAlarmCount() {
        return currentWarningCount;
    }

    public long getTotalLogRecordCount() {
        return totalLogRecordCount;
    }

    public String getCurrentAlarmCountString() {
        return Integer.toString(currentWarningCount);
    }

    public String getTotalLogRecordCountString() {
        return Long.toString(totalLogRecordCount);
    }

    @Override
    public void registerFinalEventListener(FinalEventListener listener) {
        synchronized (finalEventListeners) {
            finalEventListeners.add(listener);
        }
    }

    @Override
    public void unregisterFinalEventListener(FinalEventListener listener) {
        synchronized (finalEventListeners) {
            finalEventListeners.remove(listener);
        }
    }

    @Override
    public String getValue() {
        eventsCountPerPeriod = eventsCount;
        eventsCount = 0;
        return getEventsPerMinute();
    }

    public String getEventsPerMinute() {
        return Long.toString(eventsCountPerPeriod / EVENT_COUNT_PERIOD);
    }

    public String getLogFile() {
        return eventCountlogger.getFileName();
    }

    public void setLogFile(String logfile) {
        eventCountlogger.setFileName(logfile);
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public void setLogDirectory(String logFileDirectory) {
        this.logDirectory = logFileDirectory;
        if (!this.logDirectory.isEmpty() && !this.logDirectory.endsWith(File.pathSeparator)) {
            this.logDirectory += File.pathSeparator;
        }
    }
}
