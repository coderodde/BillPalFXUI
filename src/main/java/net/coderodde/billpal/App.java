package net.coderodde.billpal;

import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import net.coderodde.billpal.EditEvent.EditEventType;

public class App extends Application {

    private static final int WINDOW_WIDTH  = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MILLISECONDS_PER_WEEK = 7 * MILLISECONDS_PER_DAY;
    
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

    // Table menu:
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
    private final List<EditEvent> undoStack = new ArrayList<>();
    private int activeEvents;
    
    private final ListChangeListener<Bill> billListChangeListener = 
            (ListChangeListener.Change<? extends Bill> c) -> {
        c.next();
        
        if (c.wasPermutated()) {
            int[] permutation = new int[c.getTo() - c.getFrom()];
            
            for (int index = 0, i = c.getFrom(); i < c.getTo(); ++i, ++index) {
                permutation[index] = c.getPermutation(index);
            }
            
            System.out.println("From: " + c.getFrom());
            System.out.println("To  : " + c.getTo());
            System.out.println(Arrays.toString(permutation));
            
            pushEditEvent(new EditEvent(EditEventType.PERMUTE, 
                                        tableView.getItems(),
                                        permutation));
            
            editMenuUndo.setDisable(false);
        }
    };
    
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
        
        class FunkyCellFactory implements Callback<TableColumn<Bill, Date>, TableCell<Bill, Date>> {

            @Override
            public TableCell<Bill, Date> call(TableColumn<Bill, Date> column) {
                return createTableCell(column);
            }


            private TextFieldTableCell<Bill, Date> createTableCell(TableColumn<Bill, Date> col) {
                TextFieldTableCell<Bill, Date> cell = new TextFieldTableCell<Bill, Date>(new DateStringConverter()) {
                    
                    @Override
                    public void updateItem(Date date, boolean empty) {
                        super.updateItem(date, empty);
                        Bill bill = (Bill) this.getTableRow().getItem();
                        
                        if (bill == null) {
                            return;
                        }
                        
                        Date expirationDate = bill.getExpirationDate();
                        
                        if (expirationDate == null) {
                            this.setStyle("");
                            return;
                        }
                        
                        Date paymentDate = bill.getPaymentDate();
                        
                        if (paymentDate == null) {
                            long now = new Date().getTime();
                            long expirationMoment = expirationDate.getTime();
                            long millisecondsLeft = expirationMoment - now;
                            
                            String cellStyle = getCellStyle(millisecondsLeft);
                            this.setStyle(cellStyle);
                        } else {
                            // paymentDate not null here.
                            Calendar cPayment = Calendar.getInstance();
                            Calendar cExpiration = Calendar.getInstance();
                            cPayment.setTime(paymentDate);
                            cExpiration.setTime(expirationDate);
                            
                            int paymentYear = cPayment.get(Calendar.YEAR);
                            int expirationYear = cExpiration.get(Calendar.YEAR);
                            
                            if (paymentYear > expirationYear) {
                                this.setStyle("-fx-background-color: #2db6e3;");
                                return;
                            } else if (paymentYear < expirationYear) {
                                this.setStyle("-fx-background-color: #0be633");
                                return;
                            }
                            
                            int paymentMonth = cPayment.get(Calendar.MONTH);
                            int expirationMonth = 
                                    cExpiration.get(Calendar.MONTH);
                            
                            if (paymentMonth > expirationMonth) {
                                this.setStyle("-fx-background-color: #2db6e3;");
                                return;
                            } else if (paymentMonth < expirationMonth) {
                                this.setStyle("-fx-background-color: #0be633");
                                return;
                            }
                            
                            int paymentDay = 
                                    cPayment.get(Calendar.DAY_OF_MONTH);
                            int expirationDay = 
                                    cExpiration.get(Calendar.DAY_OF_MONTH);
                            
                            if (paymentDay > expirationDay) {
                                this.setStyle("-fx-background-color: #2db6e3;");
                            } else {
                                this.setStyle("-fx-background-color: #0be633");
                            }
                        }
                    }
                };
                
                return cell;
            }
        }
        
        tableColumnExpirationDate.setCellFactory(new FunkyCellFactory());

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
                        ((Bill) t.getTableView()
                                 .getItems()
                                 .get(t.getTablePosition().getRow()))
                                 .setAmount(t.getNewValue());
                    }
                }
        );

        tableColumnDateReceived.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow())).setDateReceived(t.getNewValue());
                }
            }
        );
        
        tableColumnExpirationDate.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    Bill bill = (Bill) t.getTableView()
                                        .getItems()
                                        .get(t.getTablePosition().getRow());
                    Date date = t.getNewValue();
                    
                    if (date == null) {
                        bill.setExpirationDate(null);
                        return;
                    }
                    
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    
                    bill.setExpirationDate(cal.getTime());
                }
            }
        );

        tableColumnPaymentDate.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    Bill bill = t.getTableView()
                                 .getItems()
                                 .get(t.getTablePosition()
                                 .getRow());
                    
                    bill.setPaymentDate(t.getNewValue());
                    
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
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow()))
                             .setReceiver(t.getNewValue());
                }
            }
        );

        tableColumnReceiverIban.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow()))
                             .setReceiverIban(t.getNewValue());
                }
            }
        );

        tableColumnReferenceNumber.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow()))
                             .setReferenceNumber(t.getNewValue());
                }
            }
        );

        tableColumnBillNumber.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow()))
                             .setBillNumber(t.getNewValue());
                }
            }
        );

        tableColumnComment.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, String>>() {

                @Override
                public void handle(CellEditEvent<Bill, String> t) {
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow()))
                             .setComment(t.getNewValue());
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
                addFunkyStarOnTitle();
            }
        });
        
        tableView.getItems().addListener(billListChangeListener);
    }
    
    private void addFunkyStarOnTitle() {
        stage.setTitle(stage.getTitle() + "*");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                actionClose();
            }
        });
        
        stage.setTitle("Unsaved file");
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        buildMenu();
        rootPane.setTop(menuBar);
        rootPane.setCenter(tableView);
        stage.setScene(scene);
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
                                   fileMenuAbout,
                                   fileMenuExit);

        editMenu.getItems().addAll(editMenuNewBill, editMenuRemoveSelected);
        menuBar.getMenus().addAll(fileMenu, editMenu);
    }

    private void setMenuActions() {
        fileMenuNew    .setOnAction((e) -> { actionNewDocument();  });
        fileMenuOpen   .setOnAction((e) -> { actionOpenDocument(); });
        fileMenuSave   .setOnAction((e) -> { actionSave();         });
        fileMenuSaveAs .setOnAction((e) -> { actionSaveAs();       });
        fileMenuClose  .setOnAction((e) -> { actionClose();        });
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
    
    private String getCellStyle(long millisecondsLeft) {
        if (millisecondsLeft <= 0L) {
            return "-fx-background-color: red; -fx-text-fill: black; " +
                   "-fx-font-weight: bold;";
            
        }
        
        float f = (1.0f * millisecondsLeft) / (MILLISECONDS_PER_WEEK);
        
        if (f >= 1.0f) {
            // "" means clear the table cell style, thus using default colors.
            return "";
        }
        
        int r = 255;
        int g = (int)(255 * f);
        int b = (int)(255 * f);
        
        StringBuilder sb = new StringBuilder("-fx-background-color: #");
        sb.append(Integer.toHexString(r));
        sb.append(handleLeadingZero(Integer.toHexString(g)));
        sb.append(handleLeadingZero(Integer.toHexString(b)));
        sb.append("; -fx-text-fill: black; -fx-font-weight: bold;");
        return sb.toString(); 
    }
    
    private String handleLeadingZero(String s) {
        switch (s.length()) {
            case 1:
                return "0" + s;
                
            case 2:
                return s;
                
            default:
                throw new IllegalStateException(
                        "Should not get here. Please debug.");
        }
    }
    
    private void saveFile(File file) {
        try {
            BillListWriter writer =
                    new BillListWriter(new FileOutputStream(file));
            writer.write(tableView.getItems());
            fileStateChanged = false;
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
        if (currentFile != null) {
            if (fileStateChanged) {
                saveFile(currentFile);
            }
            
            currentFile = null;
        } else {
            if (fileStateChanged) {
                saveAs();
            }
        }
        
        tableView.getItems().clear();
        fileStateChanged = false;
        stage.setTitle("Unsaved file");
    }
    
    private void actionOpenDocument() {
        if (currentFile != null) {
            if (fileStateChanged) {
                saveFile(currentFile);
                fileStateChanged = false;
            }
        } else {
            if (fileStateChanged) {
                boolean doSave = askConfirmation(
                        "The current file is not saved. " +
                        "Do you want to save it?");
                
                if (doSave) {
                    saveAs();
                    fileStateChanged = false;
                }
            }
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open document");
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                BillListReader reader = 
                        new BillListReader(new FileInputStream(file));
                List<Bill> billList = reader.read();
                tableView.getItems().clear();
                tableView.getItems().addAll(billList);
                fileStateChanged = false;
                stage.setTitle(file.getName());
                currentFile = file;
            } catch (FileNotFoundException ex) {
                showErrorDialog(
                        "File access error", 
                        "File \"" + file.getAbsolutePath() + "\" seems to be " +
                        "deleted before the user pressed Open button.");
            }
        } 
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
        stage.setTitle("Unsaved file");
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
    
    private void showErrorDialog(String title, String errorMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
    
    private boolean canUndo() {
        return activeEvents > 0;
    }
    
    private boolean canRedo() {
        return activeEvents < undoStack.size();
    }
    
    private void pushEditEvent(EditEvent editEvent) {
        while (undoStack.size() > activeEvents) {
            undoStack.remove(undoStack.size() - 1);
        }
        
        undoStack.add(editEvent);
        activeEvents++;
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
        
        EditEvent editEvent = undoStack.get(--activeEvents);
        // Since we record the edit events in the list change listener, we have
        // to remove it from the list in order to not record the actual undo.
        tableView.getItems().removeListener(billListChangeListener);
        editEvent.undo();
        tableView.getItems().addListener(billListChangeListener);
        
        editMenuUndo.setDisable(!canUndo());
        editMenuRedo.setDisable(!canRedo());
    }
    
    private void redo() {
        if (!canRedo()) {
            throw new IllegalStateException(
                    "Stack has no inactive edit events.");
        }
        
        EditEvent editEvent = undoStack.get(activeEvents++);
        // Since we record the edit events in the list change listener, we have
        // to remove it from the list in order to not record the actual redo.
        tableView.getItems().removeListener(billListChangeListener);
        editEvent.redo();
        tableView.getItems().addListener(billListChangeListener);
        
        editMenuUndo.setDisable(!canUndo());
        editMenuRedo.setDisable(!canRedo());
    }
}
