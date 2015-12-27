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
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/**
 * General command executor, used where a configurable action is needed.
 * The command is a string following a specific syntax. Class may be used
 * by sub classing or aggregation.
 *
 * @author Stefan Str�mberg
 */
public class CommandLineExecutor {

    protected HomeService server;
    protected boolean subscriptionActivated = false;
    protected boolean allowExec = false;

    /**
     * Default constructor. This should ONLY be used when the CommandLineExecutor
     * is inherited. In this case it is important that the m_EventBroker and
     * server are initialized.
     */
    protected CommandLineExecutor(HomeService server) {
        super();
        this.server = server;
    }

    /**
     * Create an instance and specify if the "exec"-command should be allowed.
     *
     * @param allowExec If this is false, the "exec"-command is disabled.
     */
    public CommandLineExecutor(HomeService server, boolean allowExec) {
        super();
        this.allowExec = allowExec;
        this.server = server;
    }

    /**
     * Executes the specified command line. The command syntax is specified
     * on: {@see <a href="http://wiki.nethome.nu/doku.php?id=commandportsyntax" />}.
     *
     * @param line Command line to be executed
     * @return The result of the command execution.
     */
    public String executeCommandLine(String line) {
        List<String> list = CommandLineParser.parseLine(line);
        Iterator<String> it = list.iterator();
        String result;
        // Check if there is a command at all, if not - that ok, just return
        if (!it.hasNext()) {
            return "ok";
        }
        // Get command and see if it is one without instance argument
        String command = it.next();
        if (command.equalsIgnoreCase("event")) {
            return performSend(it);
        }
        if (command.equalsIgnoreCase("quit")) {
            return null;
        }
        if (command.equalsIgnoreCase("dir")) {
            return performDir();
        }
        if (command.equalsIgnoreCase("subscribe")) {
            return performSubscribe();
        }
        if (command.equalsIgnoreCase("unsubscribe")) {
            return performUnsubscribe();
        }
        if (command.equalsIgnoreCase("exec") && allowExec) {
            return performExec(it);
        }
        if (command.equalsIgnoreCase("create")) {
            return performCreate(it);
        }
        if (command.equalsIgnoreCase("rename")) {
            return performRename(it);
        }
        if (command.equalsIgnoreCase("python")) {
            return executePython(it);
        }
        // Ok, it must be one of the commands which require an instance, get the instance
        if (!it.hasNext()) {
            return "error,2,No Instance specified";
        }
        String instance = it.next();

        // Try to open the specified instance
        HomeItemProxy meta;
        meta = server.openInstance(instance);
        if (meta == null) {
            return "error,3,Could not find instance specified";
        }
        if (command.equalsIgnoreCase("get")) {
            result = performGet(it, meta);
        } else if (command.equalsIgnoreCase("set")) {
            result = performSet(it, meta);
        } else if (command.equalsIgnoreCase("call")) {
            if (it.hasNext()) {
                result = "ok";
                String action = it.next();
                String value;
                try {
                    value = meta.callAction(action);
                } catch (ExecutionFailure e) {
                    return "error,10," + e.getMessage();
                }
                result += "," + value;
            } else {
                return "error,5,No action specified";
            }
        } else {
            return "error,6,Unknown command";
        }
        return result;
    }

    String performSend(Iterator<String> it) {
        if (!it.hasNext()) {
            return "error,9,No Event Type supplied";
        }
        Event theEvent = server.createEvent(it.next(), "");
        while (it.hasNext()) {
            String attribute = it.next();
            if (!it.hasNext()) {
                return "error,10,No attribute value for Event attribute: " + attribute;
            }
            theEvent.setAttribute(attribute, it.next());
        }
        server.send(theEvent);
        return "ok";
    }

    String performDir() {
        String result = "ok";
        for (DirectoryEntry directoryEntry : server.listInstances("")) {
            result += "," + CommandLineParser.quote(directoryEntry.getInstanceName());
        }
        return result;
    }

    String performGet(Iterator<String> it, HomeItemProxy item) {
        HomeItemModel meta = item.getModel();
        String attributeName;
        List<Attribute> attributeList;
        StringBuilder result = new StringBuilder("ok");
        if (!it.hasNext()) {
            // No attribute name supplied, get all
            attributeList = item.getAttributeValues();
            for (Attribute att : attributeList) {
                result.append(",").append(CommandLineParser.quote(att.getName())).append(",")
                        .append(CommandLineParser.quote(att.getValue()));
            }
        } else {
            while (it.hasNext()) {
                attributeName = it.next();
                result.append(",").append(CommandLineParser.quote(attributeName)).append(",")
                        .append(CommandLineParser.quote(item.getAttributeValue(attributeName)));
            }
        }
        return result.toString();
    }

    String performSet(Iterator<String> it, HomeItemProxy meta) {
        while (it.hasNext()) {
            String attribute = it.next();
            if (!it.hasNext()) {
                return "error,7,No attribute value for: " + CommandLineParser.quote(attribute);
            }
            String value = it.next();
            try {
                if (!meta.setAttributeValue(attribute, value)) {
                    return "error,8,Invalid attribute: " + CommandLineParser.quote(attribute);
                }
            } catch (IllegalValueException e) {
                return "error,9,Illegal value: " + e.getMessage();
            }
        }
        // If we get here, everything went ok!
        return "ok";
    }

    String performSubscribe() {
        subscriptionActivated = true;
        return "ok";
    }

    String performUnsubscribe() {
        subscriptionActivated = false;
        return "ok";
    }

    String performExec(Iterator<String> it) {
        if (!it.hasNext()) {
            return "error,10,No command to execute";
        }
        String commandLine = it.next();

        try {
            Runtime r = Runtime.getRuntime();
            // Run the upgrade command. If this is a Windows bat-file, you have to have one bat file which
            // does a "start" of the second real upgrade bat file.
            r.exec(commandLine);
        } catch (IOException e) {
            return "error,11,Could not execute command: " + commandLine;
        }
        return "ok";
    }
    
    String executePython(Iterator<String> it) {
        if (!it.hasNext()) {
            return "error,16,Missing function name";
        }
        
        String commandLine = it.next();
        while (it.hasNext())
        {
            commandLine = commandLine.concat(",").concat(it.next());
        }

        if (server.executePython(commandLine))
        {
            return "ok";
        }
        return "error,17,Could not execute function: " + commandLine;
    }

    String performCreate(Iterator<String> it) {
        String className;
        String instanceName;

        className = it.next();
        if (className == null) {
            return "error,2,No Instance name specified";
        }
        instanceName = it.next();
        if (instanceName == null) {
            return "error,14,No Class name specified";
        }
        if (server.createInstance(className, instanceName) == null) {
            return "error,15,Could not create instance";
        }
        return "ok";
    }


    String performRename(Iterator<String> it) {
        String fromName;
        String toName;

        fromName = it.next();
        if (fromName == null) {
            return "error,2,No Instance specified";
        }
        toName = it.next();
        if (toName == null) {
            return "error,12,No new name specified";
        }
        if (!server.renameInstance(fromName, toName)) {
            return "error,13,Failed to rename from " + fromName + " to " + toName;
        }
        return "ok";
    }
}