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
        REMOVE_ROWS
    }
    
    private static final class RowHolder implements Comparable<RowHolder> {

        private final int itemIndex;
        private final Bill item;
        
        RowHolder(int itemIndex, Bill item) {
            this.itemIndex = itemIndex;
            this.item = item;
        }
        
        int getItemIndex() {
            return itemIndex;
        }
        
        Bill getItem() {
            return item;
        }
        
        @Override
        public int compareTo(RowHolder o) {
            return this.itemIndex - o.itemIndex;
        }
    }
    
    private EditEventType editEventType;
    private Bill editedBill;
    private List<RowHolder> rowHolderList;
    private final List<Bill> items;
    
    public EditEvent(EditEventType editEventType, 
                     List<Bill> itemList,
                     Bill editedBill,
                     List<Integer> indexList,
                     List<Bill> billList) {
        this.editEventType = 
                Objects.requireNonNull(editEventType, 
                                       "Unknown edit event type: " +
                                       editEventType);
        
        this.items = Objects.requireNonNull(itemList, "The item list is null.");
        
        switch (editEventType) {
            case ADD_NEW_ROW:
                break;
                
            case EDIT_CELL:
                this.editedBill = 
                        Objects.requireNonNull(
                                editedBill,
                                "Edited bill is null.");
                
            case REMOVE_ROWS:
                if (indexList.size() != billList.size()) {
                    throw new IllegalStateException(
                            "The number of indices is not the same as the " +
                            "number of bills.");
                }
            
                for (int i = 0; i < indexList.size(); ++i) {
                    rowHolderList.add(new RowHolder(indexList.get(i),
                                                    billList.get(i)));
                }
        }
    }
    
    public void undo() {
        switch (editEventType) {
            
        }
    }
    
    public void redo() {
        switch (editEventType) {
            
        }
    }
}
