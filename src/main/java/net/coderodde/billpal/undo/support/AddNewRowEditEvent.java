package net.coderodde.billpal.undo.support;

import net.coderodde.billpal.App;
import net.coderodde.billpal.Bill;
import net.coderodde.billpal.undo.AbstractEditEvent;

/**
 * This class encapsulates information regarding the event of adding a new row
 * to the table.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2016)
 */
public class AddNewRowEditEvent extends AbstractEditEvent {

    public AddNewRowEditEvent(App app, boolean eventAfterSave) {
        super(app, eventAfterSave);
    }
    
    @Override
    public void undo() {
        app.getItems().remove(app.getItems().size() - 1);
    }

    @Override
    public void redo() {
        app.getItems().add(new Bill());
    }
}
