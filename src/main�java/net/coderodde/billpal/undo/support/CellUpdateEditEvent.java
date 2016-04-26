package net.coderodde.billpal.undo.support;

import java.util.Objects;
import net.coderodde.billpal.App;
import net.coderodde.billpal.Bill;
import net.coderodde.billpal.undo.AbstractEditEvent;

/**
 * This class encapsulates the information regarding the cell update edit event.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2016)
 */
public class CellUpdateEditEvent extends AbstractEditEvent {

    private final Bill before;
    private final Bill after;
    private final Bill targetBill;
    
    public CellUpdateEditEvent(App app, 
                               Bill before, 
                               Bill after,
                               Bill targetBill) {
        super(app);
        this.before = Objects.requireNonNull(
                before, 
                "The prior state of the row is null.");
        
        this.after = Objects.requireNonNull(
                after, 
                "The resultant state is null.");
        
        this.targetBill = Objects.requireNonNull(targetBill, 
                                                 "The target bill is null.");
    }
    
    @Override
    public void undo() {
        targetBill.set(before);
    }

    @Override
    public void redo() {
        targetBill.set(after);
    }
}
