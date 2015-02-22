package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.Event;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ItemEvent {
    private Event event;
    private String content;
    private Date received;
    private long id;
    private boolean wasHandled;
    private static Long idCounter = (long) 0;

    public ItemEvent(Event event, String content, boolean wasHandled) {
        id = getNewId();
        updateEvent(event, wasHandled);
        this.content = content;
    }

    public void updateEvent(Event event, boolean wasHandled) {
        this.event = event;
        this.wasHandled = wasHandled;
        received = new Date();
    }

    private static long getNewId() {
        synchronized (idCounter) {
            idCounter++;
            return idCounter;
        }
    }

    public Event getEvent() {
        return event;
    }

    public Date getReceived() {
        return received;
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    public boolean getWasHandled() {
        return wasHandled;
    }
}
