package scenarii.model;

import java.util.LinkedList;

/**
 * Created by geoffrey on 30/05/2016.
 */
public enum ActionType {
    NOACTION("No action"),
    SIMPLETEXTINSERT("Simple text insert"),
    KEYPRESS("Keyboard key(s)"),
    LEFTCLICK("Left click"),
    DBLCLICK("Double left click"),
    RIGHTCLICK("Right click"),
    DBLRIGHTCLICK("Double right click"),
    CENTERCLICK("Click on the wheel"),
    SCROLL("Scroll"),
    PINCH("Pinch or Zoom"),
    DRAGANDDROP("Drag and "),
    HOLDLEFTCLICK("Long left click"),
    HOLDRIGHTCLICK("Long right click"),
    ROTATE("Rotate on touchpad"),  // on touchpad
    SWIPELEFT("Swipe to the left"),
    SWIPE2LEFT("Swipe to the left (2 fingers)"),  // swipe with 2 fingers TO the left
    SWIPE3LEFT("Swipe to the left (3 fingers)"),
    SWIPE4LEFT("Swipe to the left (4 fingers)"),
    SWIPERIGHT("Swipe to the right"),
    SWIPE2RIGHT("Swipe to the right (2 fingers)"),  // swipe with 2 fingers TO the right
    SWIPE3RIGHT("Swipe to the right (3 fingers)"),
    SWIPE4RIGHT("Swipe to the right (4 fingers)"),
    FORCECLICK("Force Touch click (Apple)")    // Force Touch (Apple)
    ;

    private String text;

    private ActionType(final String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
