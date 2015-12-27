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

package nu.nethome.home.items.net;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * HomeItem class which listens for TCP/IP-connections on the specified port and sends
 * the content of the message as an event of type <b>TCPMessage</b> and the message in the
 * <b>Value</b>-attribute.
 * @author Stefan
 */
@Plugin
@HomeItemType("Ports")
public class TCPCommandPort extends HomeItemAdapter implements HomeItem, Runnable {

	/**
	 * Represents a session with a connected TCP-Client. 
	 */
	class Session extends CommandLineExecutor implements Runnable {
		
		protected Socket socket;
		protected boolean sessionIsRunning = true;
		protected String remoteAddress;
		
		/**
		 * Creates a new session which immediately will start listening
		 * for commands on the given socket. It starts its own listening thread
		 * which ends when the connection is ended, or the stop-method is called.
         * @param server Broker to use for command execution
         * @param socket A connected socket where the commands are read
         */
		public Session(HomeService server, Socket socket) {
			super(server, false);
            this.server = server;
			this.socket = socket;
			remoteAddress = this.socket.getInetAddress().getHostAddress();
			Thread me = new Thread(this, "TCPCommandThread");
			me.start();
		}
		
		/**
		 * Stop this session, close the connection. The session will remove
		 * itself from the sessions list when it is done.
		 */
		public void stop() {
			sessionIsRunning = false;
			try {
				socket.close();
			} catch (IOException e) {
				// Not much to do about it...
			}
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.
						getInputStream()));
				String inString;
				while(sessionIsRunning && (inString = in.readLine()) != null) {
					logger.finer("Received: " + inString + " from " + socket.getRemoteSocketAddress().toString());
					String result = executeCommandLine(inString);
					if (result == null){
						break;
					}
					result += "\n\r";
					socket.getOutputStream().write(result.getBytes());
					messageCount++;
				}
				socket.close();
			}
			catch (Exception e) {
				if (sessionIsRunning){
					logger.warning("Failed reading from socket in TCPCommandPort " + e);
					try {
						socket.close();
					} catch (IOException e1) {
						// OK, close failed - not much to do about it...
					}
				}
			}
			sessionIsRunning = false;
			// We are done closing down actions, remove us from list of active
			// sessions
			sessions.remove(this);
			logger.info("Disconnected from " + remoteAddress);
		}
		
		public int receiveEvent(Event event) {
			try {
				if ((sessionIsRunning == true) && subscriptionActivated) {
					String ev = event.toString();
					ev += "\n\r";
					socket.getOutputStream().write(ev.getBytes());
				}
			}
			catch (IOException io) {
				// Exception in write, close down
				logger.warning("Failed writing to socket in TCPCommandPort " + io);
				sessionIsRunning = false;
				try {
					socket.close();
				} catch (IOException e1) {
					// OK, close failed - not much to do about it...
				}
			}
			return 0;
		}

	}

	private static final int MAX_QUEUE_SIZE = 20;
	
	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"TCPCommandPort\" Category=\"Ports\" >"
			+ "  <Attribute Name=\"ListenPort\" Type=\"String\" Get=\"getListenPort\" Init=\"setListenPort\" Default=\"true\" />"
			+ "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
			+ "  <Attribute Name=\"SessionCount\" Type=\"String\" Get=\"getSessionCount\" />"
			+ "</HomeItem> "); 

	/*
	 * Externally visible attributes
	 */ 
	protected int listenPort = 8005;
	protected int messageCount = 0;

	/*
	 * Internal attributes
	 */
	private static final String QUIT_EVENT = "CommanderQuitEvent";
	private static Logger logger = Logger.getLogger(TCPCommandPort.class.getName());
	protected Thread listenThread;
	protected volatile boolean isRunning = false;
	protected ServerSocket serverSocket = null;
	protected SimpleDateFormat formatter = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss;");
	protected List<Session> sessions = Collections.synchronizedList(new LinkedList<Session>());
	protected LinkedBlockingQueue<Event> eventQueue;
	private Thread eventThread;
	
	public TCPCommandPort() {
		eventQueue = new LinkedBlockingQueue<Event>(MAX_QUEUE_SIZE);
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

    @Override
    protected boolean isActivated() {
        return isRunning;
    }

    /* (non-Javadoc)
         * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
         */
	public boolean receiveEvent(Event event) {
        if (!isActivated()) {
            return false;
        }
		// Put the Event in the distribution queue
		if (!eventQueue.offer(event)) {
			logger.warning("TCPCommandPort could not send event, TCP stream blocked");
		}
		return false;
	}
	
	/**
	 * Event distribution task. Reads Events from the queue and distributes it to all
	 * open sessions
	 */
	protected void eventDistributorTask() {
		while (true) {
			Event event;
			try {
				// Take the next event from the queue, will wait if no events yet
				event = eventQueue.take();
				// Check if it was the quit event, quit in that case exit and terminate the thread
				if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(QUIT_EVENT)) return;
				// Loop over all Items and offer the event
				for (Session s : sessions) {
					s.receiveEvent(event);
				}
			} catch (InterruptedException e) {
				// Do Dinada
			}
		}
	}

	public void activate(HomeService service) {
        super.activate(service);
		listenThread = new Thread(this, "TCPListenThread");
		listenThread.start();
		eventThread = new Thread("CommandPortEventDistributor") {
			@Override
			public void run(){
				eventDistributorTask();
			}
		};
		eventThread.start();
	}

	public void stop() {
		isRunning = false;

		// Close listening socket
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} 
		catch (IOException e){
			logger.warning(e.toString());
		}
		
		// Stop the event distribution thread by sending the quit event
		Event quitEvent = server.createEvent(QUIT_EVENT, "");
        eventQueue.clear();
        eventQueue.offer(quitEvent);

		// Stop all open sessions
		LinkedList<Session> temp = new LinkedList<Session>(sessions);
		for (Session s : temp) {
			s.stop();
		}
        super.stop();
	}

	/*
	 * Internal implementation methods
	 */ 
	
	/**
	 * @return Returns the listenPort.
	 */
	public String getListenPort() {
		return String.valueOf(listenPort);
	}

	public void setListenPort(String lp) {
		listenPort = Integer.parseInt(lp);
	}

	public void run() {
		try {
            isRunning = true;
            serverSocket = new ServerSocket(listenPort);
			while (isRunning) {
				try {
					// Wait for a new connection
					Socket inSocket = serverSocket.accept();
					
					// Got a new connection, create a new session and register it
					logger.info("Connection from " + inSocket.getInetAddress().getHostAddress());
					Session newSession = new Session(server, inSocket);
					sessions.add(newSession);
				}
				catch (Exception e) {
					if (isRunning){
						logger.warning("Failed reading from socket in TCPCommandPort " + e);
					}
				}
			}
		}
		catch (Exception e) {
			logger.warning("Failed creating socket in TCPCommandPort " + e);
		}
	}
	
	/**
	 * @return Returns the messageCount.
	 */
	public String getMessageCount() {
		return String.valueOf(messageCount);
	}
	
	/**
	 * @return number of active sessions
	 */
	public String getSessionCount(){
		return String.valueOf(sessions.size());
	}
}

