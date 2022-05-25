package g3.project.network;

import java.io.Serializable;

import g3.project.core.Scripting;
import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;


public class SessionPacket implements Serializable {
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
     * Session packet constructor for MouseEvent
     * 
     * @param event
     * @param elID
     * @param ScrType
     * @param mouseButton
     * @param x
     * @param y
     * @param down
     */
    public SessionPacket(final String currentPageID, final MouseEvent mev, final String elID) {
        this.currentPageID = currentPageID;
        this.elID = elID;
        this.x = mev.getX();
        this.y = mev.getY();
        final var evType = mev.getEventType();
        if (evType == MouseEvent.MOUSE_PRESSED || evType == MouseEvent.MOUSE_RELEASED || evType == MouseEvent.MOUSE_CLICKED) {
            this.down = (mev.getEventType() == MouseEvent.MOUSE_PRESSED); //Is the mouse pressed right now?
            this.ScrType = Scripting.CLICK_FN;
            this.mouseButton = mev.getButton();
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

    /**
     * Session packet constructor for any other event
     * @param currentPageID
     * @param event
     */
    public SessionPacket(String currentPageID, Event event){
        this.event = event;
        this.currentPageID = currentPageID;
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
}
