package g3.project.network;

import java.io.Serializable;

import javafx.event.Event;


public class SessionPacket implements Serializable {
    private String currentPageID = "";
    private Event event = null;

    public SessionPacket(String currentPageID, Event eventToSend){
        this.currentPageID = currentPageID;
        this.event = eventToSend;
    }

    public String getCurrentPageID(){
        return currentPageID;
    }

    public Event getEvent(){
        return event;
    }
}
