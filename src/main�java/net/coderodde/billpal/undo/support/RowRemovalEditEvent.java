package net.coderodde.billpal.undo.support;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.coderodde.billpal.App;
import net.coderodde.billpal.Bill;
import net.coderodde.billpal.undo.AbstractEditEvent;

/**
 * This class encapsulates information needed for undo-/redoing row removal
 * edit.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2016)
 */
public class RowRemovalEditEvent extends AbstractEditEvent {

    private final TreeMap<Integer, Bill> removalMap;
    
    // The constructor expects a TreeMap so that the order can be reconstructed.
    public RowRemovalEditEvent(App app, 
                               boolean eventAfterSave,
                               TreeMap<Integer, Bill> removalMap) {
        super(app);
        this.removalMap = Objects.requireNonNull(
                removalMap, "The removal map is null.");
    }
    
    @Override
    public void undo() {
        for (Map.Entry<Integer, Bill> entry : removalMap.entrySet()) {
            app.getItems().add(entry.getKey(), entry.getValue());
        }
        
        app.rebuildBillListIndexMap();
    }

    @Override
    public void redo() {
        for (Integer index : removalMap.descendingKeySet()) {
            int idx = index;
            app.getItems().remove(idx);
        }
        
        app.rebuildBillListIndexMap();
    }
}
