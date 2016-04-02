package net.coderodde.billpal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class describes edit operations made to the bill list via a table view.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 31, 2016)
 */
public class EditEvent {
    
    public enum EditEventType {
        ADD_NEW_ROW,
        EDIT_CELL,
        PERMUTE,
        REMOVE_ROWS
    }
    
    private final EditEventType editEventType;
    private final List<Bill> tableItems;
    private final int[] permutation;
    
    public EditEvent(EditEventType editEventType, 
                     List<Bill> tableItems, 
                     int[] permutation) {
        this.editEventType = 
                Objects.requireNonNull(editEventType, 
                                       "Unknown edit event type: " +
                                       editEventType);
        this.tableItems = tableItems;
        this.permutation = permutation;
    }
    
    public void undo() {
        switch (editEventType) {
            case PERMUTE:
                
                break;
        }
    }
    
    public void redo() {
        switch (editEventType) {
            case PERMUTE:
                
                break;
        }
    }
}
