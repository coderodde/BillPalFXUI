package net.coderodde.billpal.undo.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.coderodde.billpal.App;
import net.coderodde.billpal.Bill;
import net.coderodde.billpal.undo.AbstractEditEvent;

/**
 * This class encapsulates information regarding a row rearrangement operation.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2016)
 */
public class PermuteEditEvent extends AbstractEditEvent {

    private final int[] permutation;
    
    public PermuteEditEvent(App app, 
                            boolean eventAfterSave, 
                            int[] permutation) {
        super(app);
        this.permutation = Objects.requireNonNull(
                permutation,
                "The permutation map is null.");
    }
    
    @Override
    public void undo() {
        List<Bill> permutationList = new ArrayList<>(app.getItems());
        
        for (int index = 0; index < permutation.length; ++index) {
            permutationList.set(index, app.getItems().get(permutation[index]));
        }
    
        app.getItems().clear();
        app.getItems().addAll(permutationList);
    }

    @Override
    public void redo() {
        List<Bill> permutationList = new ArrayList<>(app.getItems());
        
        for (int index = 0; index < permutation.length; ++index) {
            permutationList.set(permutation[index], app.getItems().get(index));
        }
        
        app.getItems().clear();
        app.getItems().addAll(permutationList);
    }
}
