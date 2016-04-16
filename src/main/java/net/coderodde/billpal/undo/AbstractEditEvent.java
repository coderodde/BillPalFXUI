package net.coderodde.billpal.undo;

import java.util.Objects;
import net.coderodde.billpal.App;

/**
 * This abstract class defines the API for edit events.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2016)
 */
public abstract class AbstractEditEvent {
   
    protected final App app;
    protected boolean eventBeforeSave;
    protected boolean eventAfterSave;
    
    public AbstractEditEvent(App app) {
        this.app = Objects.requireNonNull(app, 
                                          "The Application instance is null.");
    }
    
    public abstract void undo();
    
    public abstract void redo();
    
    public void setEventBeforeSave(boolean eventBeforeSave) {
        this.eventBeforeSave = eventBeforeSave;
    }
    
    public void setEventAfterSave(boolean eventAfterSave) {
        this.eventAfterSave = eventAfterSave;
    }
    
    public boolean eventIsBeforeSave() {
        return eventBeforeSave;
    }
    
    public boolean eventIsAfterSave() {
        return eventAfterSave;
    }
}
