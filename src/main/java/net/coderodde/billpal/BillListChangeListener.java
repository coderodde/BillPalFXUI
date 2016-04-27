package net.coderodde.billpal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import javafx.collections.ListChangeListener;
import net.coderodde.billpal.undo.support.AddNewRowEditEvent;
import net.coderodde.billpal.undo.support.PermuteEditEvent;
import net.coderodde.billpal.undo.support.RowRemovalEditEvent;

/**
 * This class implements the bill list change listener.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 8, 2016)
 */
class BillListChangeListener implements ListChangeListener<Bill> {

    private final App app;
    
    BillListChangeListener(App app) {
        this.app = Objects.requireNonNull(app,
                                          "The application reference is null.");
    }
    
    @Override
    public void onChanged(Change<? extends Bill> c) {
        c.next();

        if (c.wasPermutated()) {
            int[] permutation = new int[c.getTo() - c.getFrom()];

            for (int index = 0, i = c.getFrom(); i < c.getTo(); ++i, ++index) {
                permutation[index] = c.getPermutation(index);
            }

            app.pushEditEvent(new PermuteEditEvent(app, false, permutation));
            app.getBillIndexMap().clear();
            List<Bill> billList = app.getItems();
            Map<Bill, Integer> billIndexMap = app.getBillIndexMap();
            
            for (int index = 0; index < billList.size(); ++index) {
                billIndexMap.put(billList.get(index), index);
            }
        } else if (c.wasAdded()) {
            app.pushEditEvent(new AddNewRowEditEvent(app));

            int billListSize = app.getItems().size();
            app.getBillIndexMap().put(app.getItems().get(billListSize - 1),
                    billListSize - 1);
        } else if (c.wasRemoved()) {
            Set<Bill> removeSet = new HashSet<>(c.getRemoved());

            while (c.next()) {
                removeSet.addAll(c.getRemoved());
            }

            TreeMap<Integer, Bill> map = new TreeMap<>();

            for (Bill removedBill : removeSet) {
                map.put(app.getBillIndexMap().get(removedBill), removedBill);
            }

            app.pushEditEvent(new RowRemovalEditEvent(app, false, map));

            // Recompute the index map.
            app.rebuildBillListIndexMap();
        }

        app.setUndoEditMenuDisabled(!app.canUndo());
        app.setRedoEditMenuDisabled(!app.canRedo());
        
        app.onUpdate();
    }
}
