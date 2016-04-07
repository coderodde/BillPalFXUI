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
    protected final boolean eventAfterSave;
    
    public AbstractEditEvent(App app, boolean eventAfterSave) {
        this.app = Objects.requireNonNull(app, 
                                          "The Application instance is null.");
        this.eventAfterSave = eventAfterSave;
    }
    
    public abstract void undo();
    
    public abstract void redo();
}
