package net.coderodde.billpal;

import net.coderodde.billpal.fileio.BillListWriter;
import net.coderodde.billpal.fileio.BillListReader;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.coderodde.billpal.undo.AbstractEditEvent;
import net.coderodde.billpal.undo.support.CellUpdateEditEvent;

public class App extends Application {

    private static final int WINDOW_WIDTH  = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String UNSAVED_FILE_TITLE = "Unsaved file";
    
    private final TableView<Bill> tableView = new TableView<>();
    private Stage stage;
    
    // Table columns:
    private final TableColumn<Bill, Date>   tableColumnExpirationDate;
    private final TableColumn<Bill, Date>   tableColumnPaymentDate;
    private final TableColumn<Bill, Double> tableColumnAmount;
    private final TableColumn<Bill, Date>   tableColumnDateReceived;
    private final TableColumn<Bill, String> tableColumnReceiver;
    private final TableColumn<Bill, String> tableColumnReceiverIban;
    private final TableColumn<Bill, String> tableColumnReferenceNumber;
    private final TableColumn<Bill, String> tableColumnBillNumber;
    private final TableColumn<Bill, String> tableColumnComment;

    // Menu stuff:
    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu   = new Menu("File");
    private final Menu editMenu   = new Menu("Edit");
    private final MenuItem fileMenuNew     = new MenuItem("New");
    private final MenuItem fileMenuOpen    = new MenuItem("Open");
    private final MenuItem fileMenuSave    = new MenuItem("Save");
    private final MenuItem fileMenuSaveAs  = new MenuItem("Save as");
    private final MenuItem fileMenuClose   = new MenuItem("Close");
    private final MenuItem fileMenuAbout   = new MenuItem("About");
    private final MenuItem fileMenuExit    = new MenuItem("Exit");
    private final MenuItem editMenuNewBill = new MenuItem("New bill");
    private final MenuItem editMenuRemoveSelected =
              new MenuItem("Remove selected");
    private final MenuItem editMenuUndo = new MenuItem("Undo");
    private final MenuItem editMenuRedo = new MenuItem("Redo");

    // Table pop-up menu:
    private final MenuItem tableMenuAddRow = new MenuItem("Add new row");
    private final MenuItem tableMenuRemoveSelected = 
              new MenuItem("Remove selected");

    // Other:
    private final BorderPane rootPane = new BorderPane();
    private final Scene scene = new Scene(rootPane);

    // File state:
    private boolean fileStateChanged = false;
    private File currentFile;
    
    // Undo/redo stack stuff:
    private final List<AbstractEditEvent> undoStack = new ArrayList<>();
    private int activeEvents;
    private final Map<Bill, Integer> billIndexMap = new HashMap<>();
    
    private final ListChangeListener<Bill> listChangeListener = 
            new BillListChangeListener(this);
    
    public App() {
        this.editMenuRedo.setDisable(true);
        this.editMenuUndo.setDisable(true);
        
        this.tableColumnExpirationDate  = new TableColumn<>("Expires");
        this.tableColumnPaymentDate     = new TableColumn<>("Paid");
        this.tableColumnAmount          = new TableColumn<>("Amount");
        this.tableColumnDateReceived    = new TableColumn<>("Date received");
        this.tableColumnReceiver        = new TableColumn<>("Receiver");
        this.tableColumnReceiverIban    = new TableColumn<>("IBAN");
        this.tableColumnReferenceNumber = new TableColumn<>("Reference");
        this.tableColumnBillNumber      = new TableColumn<>("Bill number");
        this.tableColumnComment         = new TableColumn<>("Comment");

        tableColumnExpirationDate.setStyle("-fx-font-weight: bold;");
        
        tableColumnAmount.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        tableColumnDateReceived.setCellValueFactory(
                new PropertyValueFactory<>("dateReceived")
        );

        tableColumnExpirationDate.setCellValueFactory(
                new PropertyValueFactory<>("expirationDate")
        );

        tableColumnPaymentDate.setCellValueFactory(
                new PropertyValueFactory<>("paymentDate")
        );

        tableColumnReceiver.setCellValueFactory(
                new PropertyValueFactory<>("receiver")
        );
        
        tableColumnReceiverIban.setCellValueFactory(
                new PropertyValueFactory<>("receiverIban")
        );

        tableColumnReferenceNumber.setCellValueFactory(
                new PropertyValueFactory<>("referenceNumber")
        );

        tableColumnBillNumber.setCellValueFactory(
                new PropertyValueFactory<>("billNumber")
        );

        tableColumnComment.setCellValueFactory(
                new PropertyValueFactory<>("comment")
        );

        tableColumnAmount.setCellFactory(
            TextFieldTableCell.
                    <Bill, Double>forTableColumn(new DoubleStringConverter()));

        tableColumnDateReceived.setCellFactory(
            TextFieldTableCell.
                    <Bill, Date>forTableColumn(new DateStringConverter()));
        
        tableColumnExpirationDate.setCellFactory(
                new ExpirationDateCellFactory());

        tableColumnPaymentDate.setCellFactory(
            TextFieldTableCell.
                    <Bill, Date>forTableColumn(new DateStringConverter()));

        tableColumnReceiver.setCellFactory(
            TextFieldTableCell.<Bill>forTableColumn());

        tableColumnReceiverIban.setCellFactory(
            TextFieldTableCell.<Bill>forTableColumn());

        tableColumnReferenceNumber.setCellFactory(
            TextFieldTableCell.<Bill>forTableColumn());

        tableColumnBillNumber.setCellFactory(
            TextFieldTableCell.<Bill>forTableColumn());

        tableColumnComment.setCellFactory(
            TextFieldTableCell.<Bill>forTableColumn());

        tableColumnAmount.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Double>>() {

                    @Override
                    public void handle(CellEditEvent<Bill, Double> t) {
                        Bill target = t.getTableView()
                                       .getItems()
                                       .get(t.getTablePosition().getRow());
                        Bill before = new Bill(target);
                        target.setAmount(t.getNewValue());
                        Bill after = new Bill(target);
                        
                        pushEditEvent(new CellUpdateEditEvent(App.this, 
                                                              before, 
                                                              after, 
                                                              target));
                    }
                }
        );

        tableColumnDateReceived.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    Bill before = new Bill(target);
                    target.setDateReceived(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this,
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );
        
        tableColumnExpirationDate.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    Date date = t.getNewValue();
                    
                    if (date == null) {
                        target.setExpirationDate(null);
                        Bill after = new Bill(target);
                        pushEditEvent(new CellUpdateEditEvent(App.this, 
                                                              before, 
                                                              after, 
                                                              target));
                        return;
                    }
                    
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    
                    target.setExpirationDate(cal.getTime());
                    Bill after = new Bill(target);
                    pushEditEvent(new CellUpdateEditEvent(App.this, 
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );

        tableColumnPaymentDate.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    target.setPaymentDate(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this,
                                                          before, 
                                                          after, 
                                                          target));
                    
                    // A magic spell needed for updating the background color of 
                    // the expiration date cell whenever the corresponding 
                    // payment date cell is edited.
                    tableView.getProperties().put(TableViewSkinBase.RECREATE,
                                                  Boolean.TRUE);
                }
            }
        );

        tableColumnReceiver.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    target.setReceiver(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this, 
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );

        tableColumnReceiverIban.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    target.setReceiverIban(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this,
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );

        tableColumnReferenceNumber.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    target.setReferenceNumber(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this, 
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );

        tableColumnBillNumber.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    target.setBillNumber(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this,
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );

        tableColumnComment.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    Bill target = t.getTableView()
                                   .getItems()
                                   .get(t.getTablePosition().getRow());
                    
                    Bill before = new Bill(target);
                    target.setComment(t.getNewValue());
                    Bill after = new Bill(target);
                    
                    pushEditEvent(new CellUpdateEditEvent(App.this, 
                                                          before, 
                                                          after, 
                                                          target));
                }
            }
        );

        tableView.getColumns().addAll(tableColumnExpirationDate,
                                      tableColumnPaymentDate,
                                      tableColumnAmount,
                                      
                                      tableColumnDateReceived,
                                      tableColumnReceiver,
                                      tableColumnReceiverIban,
                                      
                                      tableColumnReferenceNumber,
                                      tableColumnBillNumber,
                                      tableColumnComment);

        setMenuActions();
        buildTablePopupMenu();

        tableColumnExpirationDate .setPrefWidth(WINDOW_WIDTH / 10);
        tableColumnPaymentDate    .setPrefWidth(WINDOW_WIDTH / 10);
        tableColumnAmount         .setPrefWidth(WINDOW_WIDTH / 10);

        tableColumnDateReceived   .setPrefWidth(2 * WINDOW_WIDTH / 10);
        tableColumnReceiver       .setPrefWidth(WINDOW_WIDTH / 10);
        tableColumnReceiverIban   .setPrefWidth(WINDOW_WIDTH / 10);

        tableColumnReferenceNumber.setPrefWidth(WINDOW_WIDTH / 10);
        tableColumnBillNumber     .setPrefWidth(WINDOW_WIDTH / 10);
        tableColumnComment        .setPrefWidth(WINDOW_WIDTH / 10);
        
        tableView.getItems().addListener(
                (ListChangeListener.Change<? extends Bill> c) -> {
            if (!fileStateChanged) {
                fileStateChanged = true;
                setFileSavedStatus(false);
            }
        });
        
        tableView.getItems().addListener(listChangeListener);
    }
    
    public List<Bill> getItems() {
        return tableView.getItems();
    }
    
    public TableView<Bill> getTableView() {
        return tableView;
    }
    
    public void rebuildBillListIndexMap() {
        billIndexMap.clear();
        
        int index = 0;
        
        for (Bill bill : tableView.getItems()) {
            billIndexMap.put(bill, index++);
        }
    }
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest((WindowEvent event) -> {
            actionClose();
        });
        
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        buildMenu();
        rootPane.setTop(menuBar);
        rootPane.setCenter(tableView);
        stage.setScene(scene);
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setFileSavedStatus(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void buildMenu() {
        fileMenu.getItems().addAll(fileMenuNew,
                                   fileMenuOpen,
                                   fileMenuSave,
                                   fileMenuSaveAs,
                                   fileMenuClose,
                                   new SeparatorMenuItem(),
                                   fileMenuAbout,
                                   fileMenuExit);

        editMenu.getItems().addAll(editMenuNewBill, 
                                   editMenuRemoveSelected,
                                   new SeparatorMenuItem(),
                                   editMenuUndo,
                                   editMenuRedo);
        
        menuBar.getMenus().addAll(fileMenu, editMenu);
    }

    private void setMenuActions() {
        fileMenuNew    .setOnAction((e) -> { actionNewDocument();  });
        fileMenuOpen   .setOnAction((e) -> { actionOpenDocument(); });
        fileMenuSave   .setOnAction((e) -> { actionSave();         });
        fileMenuSaveAs .setOnAction((e) -> { actionSaveAs();       });
//        fileMenuClose  .setOnAction((e) -> { actionClose();        });
        fileMenuAbout  .setOnAction((e) -> { actionAbout();        });
        fileMenuExit   .setOnAction((e) -> { actionExit();         });
        
        editMenuNewBill.setOnAction((e) -> { 
            tableView  .getItems().add(new Bill());
        });

        editMenuRemoveSelected.setOnAction((e) -> {
            List<Bill> selectedBillList = 
                    tableView.getSelectionModel().getSelectedItems();
            tableView.getItems().removeAll(selectedBillList);
            tableView.getSelectionModel().clearSelection();
        });
        
        editMenuUndo.setOnAction((e) -> { undo(); });
        editMenuRedo.setOnAction((e) -> { redo(); });
        
        tableMenuAddRow.setOnAction((e) -> {
            tableView.getItems().add(new Bill());
        });

        tableMenuRemoveSelected.setOnAction((e) -> {
            List<Bill> selectedBillList = 
                    tableView.getSelectionModel().getSelectedItems();
            tableView.getItems().removeAll(selectedBillList);
            tableView.getSelectionModel().clearSelection();
        });
    }

    private void buildTablePopupMenu() {
        tableView.setContextMenu(new ContextMenu(tableMenuAddRow,
                                                 tableMenuRemoveSelected));
    }
    
    private void saveFile(File file) {
        try {
            BillListWriter writer =
                    new BillListWriter(new FileOutputStream(file));
            writer.write(tableView.getItems());
            setFileSavedStatus(fileStateChanged = false);
        } catch (FileNotFoundException ex) {
            showErrorDialog(
                    "File access error",
                    "File \"" + file.getAbsolutePath() + "\" does not exist. " +
                    "It seems like the file was removed before you pressed " +
                    "the Save-button.");
        }
    }
    
    private File saveAs() {
        // Ask the user about new file name.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file as...");
        File file = fileChooser.showSaveDialog(stage);
        
        if (file == null) {
            return null;
        }
        
        try {
            BillListWriter writer =
                    new BillListWriter(new FileOutputStream(file));
            writer.write(tableView.getItems());
            return file;
        } catch (FileNotFoundException ex) {
            showErrorDialog("File access error",
                            "The file \"" + file.getAbsolutePath() + "\" " + 
                            "cannot be found. It looks like it was removed " +
                            "before you pressed the Save button.");
            return null;
        }
    }
    
    private void actionNewDocument() {
        if (!fileStateChanged) {
            currentFile = null;
            setFileSavedStatus(true);
            undoStack.clear();
            activeEvents = 0;
            tableView.getItems().clear();
            return;
        }
        
        // Once here, the current document was modified.
        
        if (currentFile == null) {
            ButtonType buttonType = 
                    askYesNoCancel(
                            "The current document is not saved. Save it?");

            if (buttonType == ButtonType.YES) {
                System.out.println("Yes");
                File file = saveAs();
                
                if (file == null) {
                    // Once here, the user pressed Cancel button in the Save
                    // dialog. Abort saving + creating new document and give 
                    // the user opportunity to decide.
                    return;
                }
                
                undoStack.clear();
                activeEvents = 0;
                tableView.getItems().clear();
                setFileSavedStatus(true);
            } else if (buttonType == ButtonType.NO) {
                System.out.println("No");
                undoStack.clear();
                activeEvents = 0;
                tableView.getItems().clear();
                setFileSavedStatus(true);
            } else if (buttonType == ButtonType.CANCEL) {
                
            } else {
                throw new IllegalStateException(
                        "Unrecognized button type: " + buttonType);
            }
            
            return;
        }
        
        // Once here, the current document was saved previously, i.e., it exists
        // in the file system.
        ButtonType buttonType = 
                askYesNoCancel(
                "The current file has been modified. Save the changes?");
        
        if (buttonType == ButtonType.CANCEL) {
            return;
        } 
        
        if (buttonType == ButtonType.YES) {
            saveFile(currentFile);
        } else if (buttonType == ButtonType.NO) {
            
        } else {
            throw new IllegalStateException(
                    "Unrecognized button type: " + buttonType);
        }
        
        setFileSavedStatus(true);
        undoStack.clear();
        activeEvents = 0;
        currentFile = null;
    }
    
    private void actionOpenDocument() {
        if (currentFile != null) {
            if (fileStateChanged) {
                ButtonType buttonType = 
                        askYesNoCancel("The current file is modified. " + 
                                       "Save the changes?");
                
                boolean doActualFileOpening = true;
                
                if (buttonType == ButtonType.YES) {
                    saveFile(currentFile);
                } else if (buttonType == ButtonType.NO) {
                    
                } else if (buttonType == ButtonType.CANCEL) {
                    doActualFileOpening = false;
                } else {
                    throw new IllegalStateException(
                            "Unrecognized button type: " + buttonType);
                }
                
                if (!doActualFileOpening) {
                    return;
                }
            }
        } else {
            if (fileStateChanged) {
                ButtonType buttonType = 
                        askYesNoCancel("The current file is not saved. " + 
                                       "Save it?");
                
                boolean doActualFileOpening = true;
                
                if (buttonType == ButtonType.YES) {
                    File file = saveAs();
                    
                    if (file == null) {
                        // If here, the user pressed the cancel button in the
                        // file choice dialog. Give him/her another chance for
                        // deciding what to do with the current file.
                        return;
                    }
                } else if (buttonType == ButtonType.NO) {
                    
                } else if (buttonType == ButtonType.CANCEL) {
                    doActualFileOpening = false;
                } else {
                    throw new IllegalArgumentException(
                            "Unrecognized buttont type: " + buttonType);
                }
                
                if (!doActualFileOpening) {
                    return;
                }
            }
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open document");
        File file = fileChooser.showOpenDialog(stage);
        
        if (file == null) {
            return;
        }
        
        try {
            BillListReader reader = 
                    new BillListReader(new FileInputStream(file));
            List<Bill> billList = reader.read();
            tableView.getItems().clear();
            tableView.getItems().addAll(billList);
            setFileSavedStatus(fileStateChanged = false);
            stage.setTitle(file.getName());
            currentFile = file;
        } catch (FileNotFoundException ex) {
            showErrorDialog(
                    "File access error", 
                    "File \"" + file.getAbsolutePath() + "\" seems to be " +
                    "deleted before the user pressed Open button.");
        }
        
        undoStack.clear();
        activeEvents = 0;
    }
    
    private void actionSave() {
        if (currentFile != null) {
            if (fileStateChanged) {
                saveFile(currentFile);
                stage.setTitle(currentFile.getName());
            }
        } else {
            currentFile = saveAs();
            
            if (currentFile != null) {
                stage.setTitle(currentFile.getName());
            }
        }
    }
    
    private void actionSaveAs() {
        saveAs();
    }
    
    private void actionClose() {
        if (currentFile == null) {
            if (fileStateChanged) {
                boolean doSave = askConfirmation(
                    "The current file is not saved. Save it?");
                
                if (doSave) {
                    saveAs();
                }
            }
        } else {
            boolean doUpdate = askConfirmation(
                    "The current file has been modified. Save the changes?");
            
            if (doUpdate) {
                saveFile(currentFile);
            }
        }
        
        tableView.getItems().clear();
        stage.setTitle("Unsaved file*");
        undoStack.clear();
        activeEvents = 0;
    }
    
    private void actionAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText("");
        alert.setTitle("");
        alert.setContentText( 
                "BillPal 1.6\nBy Rodion \"rodde\" Efremov, 2016.03.30");
        alert.showAndWait();
    }
    
    private void actionExit() {
        if (fileStateChanged) {
            if (currentFile != null) {
                boolean update = askConfirmation(
                        "The current file is modified. Save the changes?");
                
                if (update) {
                    saveFile(currentFile);
                }
            } else {
                boolean save = askConfirmation(
                        "The current file is not saved. Save it now?");
                
                if (save) {
                    saveAs();
                }
            }
        }
        
        System.exit(0);
    }
    
    private boolean askConfirmation(String question) {
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                                question, 
                                ButtonType.OK, 
                                ButtonType.CANCEL);
        return alert.showAndWait().get() == ButtonType.OK;
    }
    
    private ButtonType askYesNoCancel(String question) {
        Alert alert = new Alert(AlertType.CONFIRMATION,
                                question,
                                ButtonType.YES,
                                ButtonType.NO,
                                ButtonType.CANCEL);
        return alert.showAndWait().get();
    }
    
    private void showErrorDialog(String title, String errorMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
    
    boolean canUndo() {
        return activeEvents > 0;
    }
    
    boolean canRedo() {
        return activeEvents < undoStack.size();
    }
    
    Map<Bill, Integer> getBillIndexMap() {
        return billIndexMap;
    }
    
    void setUndoEditMenuDisabled(boolean disabled) {
        editMenuUndo.setDisable(disabled);
    }
    
    void setRedoEditMenuDisabled(boolean disabled) {
        editMenuRedo.setDisable(disabled);
    }
    
    void pushEditEvent(AbstractEditEvent editEvent) {
        while (undoStack.size() > activeEvents) {
            undoStack.remove(undoStack.size() - 1);
        }
        
        undoStack.add(editEvent);
        activeEvents++;
        
        editMenuUndo.setDisable(!canUndo());
        editMenuRedo.setDisable(!canRedo());
    }
    
    private void undo() {
        if (!canUndo()) {
            if (undoStack.isEmpty()) {
                throw new IllegalStateException(
                        "The edit event stack is empty.");
            }
            
            if (activeEvents == 0) {
                throw new IllegalStateException(
                        "The edit stack contains no active events.");
            }
        }
        
        AbstractEditEvent editEvent = undoStack.get(--activeEvents);
        // Since we record the edit events in the list change listener, we have
        // to remove it from the list in order to not record the actual undo.
        tableView.getItems().removeListener(listChangeListener);
        editEvent.undo();
        tableView.getItems().addListener(listChangeListener);
        
        editMenuUndo.setDisable(!canUndo());
        editMenuRedo.setDisable(!canRedo());
    }
    
    private void redo() {
        if (!canRedo()) {
            throw new IllegalStateException(
                    "Stack has no inactive edit events.");
        }
        
        AbstractEditEvent editEvent = undoStack.get(activeEvents++);
        
        if (editEvent instanceof CellUpdateEditEvent) {
            System.out.println(editEvent);
        }
        
        // Since we record the edit events in the list change listener, we have
        // to remove it from the list in order to not record the actual redo.
        tableView.getItems().removeListener(listChangeListener);
        editEvent.redo();
        tableView.getItems().addListener(listChangeListener);
        
        editMenuUndo.setDisable(!canUndo());
        editMenuRedo.setDisable(!canRedo());
    }
    
    private void setFileSavedStatus(boolean saved) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentFile == null ? "Unsaved file" : currentFile.getName());
        
        if (!saved) {
            sb.append(" - Edited");
        }
        
        stage.setTitle(sb.toString());
        fileStateChanged = !saved;
    }
}
