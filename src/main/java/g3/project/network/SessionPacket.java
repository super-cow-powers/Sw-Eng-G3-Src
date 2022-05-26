package g3.project.network;

import java.io.Serializable;

import g3.project.core.Scripting;
import javafx.event.Event;
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
    private String ScrType = "";
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
        if(!(event.getClass().equals(MouseEvent.class))){
            this.event = event;
        } else{
            this.elID = ((javafx.scene.Node)event.getSource()).getId();
            this.x = ((MouseEvent)event).getX();
            this.y = ((MouseEvent)event).getY();
            final var evType = event.getEventType();
            if (evType == MouseEvent.MOUSE_PRESSED || evType == MouseEvent.MOUSE_RELEASED || evType == MouseEvent.MOUSE_CLICKED) {
                this.down = (event.getEventType() == MouseEvent.MOUSE_PRESSED); //Is the mouse pressed right now?
                this.ScrType = Scripting.CLICK_FN;
                this.mouseButton = ((MouseEvent)event).getButton();
            } else if (evType == MouseEvent.MOUSE_MOVED) {
                this.ScrType = Scripting.MOUSE_MOVED_FN;
            } else if (evType == MouseEvent.MOUSE_ENTERED) {
                this.ScrType = Scripting.MOUSE_ENTER_FN;
            } else if (evType == MouseEvent.MOUSE_EXITED) {
                this.ScrType = Scripting.MOUSE_EXIT_FN;
            } else if (evType == MouseEvent.MOUSE_DRAGGED) {
                this.ScrType = Scripting.DRAG_FUNCTION;
            }
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

    public Event getEvent(){
        return event;
    }

    public String getElID() {
        return elID;
    }

    public String getScrType() {
        return ScrType;
    }

    public MouseButton getMouseButton() {
        return mouseButton;
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
