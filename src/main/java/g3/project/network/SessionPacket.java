package g3.project.network;

import java.io.Serializable;
import java.util.Optional;

import g3.project.core.Scripting;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


public class SessionPacket implements Serializable {
    private static final long serialVersionUID = 206007353L;
    private String currentPageID = "";
    private Event event = null;
    /**
     * Element ID
     */
    private String elID = "";
    /**
     * Scripting Event type
     */
    private EventType<? extends Event> mevType = null;
    /**
     * Mouse button related to the MouseEvent
     */
    private MouseButton mouseButton = null;
    /**
     * Mouse x position
     */
    private double x = 0;
    /**
     * Mouse y position
     */
    private double y = 0;
    /**
     * Mouse click condition
     */
    private boolean down = false;
    /**
     * Connection Text
     */
    private String connectionText = "";

    /**
     * Session packet constructor for any other event
     * @param currentPageID
     * @param event
     */
    public SessionPacket(String currentPageID, Event event){
        if(!(event instanceof MouseEvent)){
            this.event = event;
        } else{
            this.elID = ((javafx.scene.Node)event.getSource()).getId();
            this.x = ((MouseEvent)event).getX();
            this.y = ((MouseEvent)event).getY();
            this.down = (event.getEventType() == MouseEvent.MOUSE_PRESSED); //Is the mouse pressed right now?
            this.mevType = event.getEventType();
            this.mouseButton = ((MouseEvent)event).getButton();
        }
        this.currentPageID = currentPageID;
    }

    /**
     * Session packet constructor for connection verification
     * @param connectionText
     */
    public SessionPacket(String connectionText){
        this.connectionText = connectionText;
    }

    public String getCurrentPageID(){
        return currentPageID;
    }

    public Optional<Event> getEvent(){
        return Optional.ofNullable(event);
    }

    public String getElID() {
        return elID;
    }

    public Optional<?> getMevType() {
        return Optional.ofNullable(mevType);
    }

    public Optional<MouseButton> getMouseButton() {
        return Optional.ofNullable(mouseButton);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isDown() {
        return down;
    }

    public String getConnectionText() {
        return connectionText;
    }
}
