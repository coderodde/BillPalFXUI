package net.coderodde.billpal;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.util.Callback;

public class App extends Application {

    private static final int WINDOW_WIDTH  = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

    private final TableView<Bill> tableView = new TableView<>();

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

    // Table menu:
    private final MenuItem tableMenuAddRow = new MenuItem("Add new row");
    private final MenuItem tableMenuRemoveSelected = 
              new MenuItem("Remove selected");

    // Other:
    private final BorderPane rootPane = new BorderPane();
    private final Scene scene = new Scene(rootPane);

    public App() {
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
                            System.out.println("Expiration date is null.");
                            return;
                        }
                        
                        Date paymentDate = bill.getPaymentDate();
                        
                        if (paymentDate == null) {
                            long now = new Date().getTime();
                            now -= now % MILLISECONDS_PER_DAY;
                            
                            long expirationMoment = expirationDate.getTime();
                            expirationMoment -= expirationMoment % 
                                                MILLISECONDS_PER_DAY;
                            expirationMoment += MILLISECONDS_PER_DAY;
                            
                            long daysLeft = (expirationMoment - now) /
                                            MILLISECONDS_PER_DAY;
                            
                            String cellStyle = getCellStyle(daysLeft);
                            this.setStyle(cellStyle);
                            System.out.println("style: " + cellStyle);
                        } else {
                            // paymentDate not null here.
                            Calendar cPayment = Calendar.getInstance();
                            Calendar cExpiration = Calendar.getInstance();
                            cPayment.setTime(paymentDate);
                            cExpiration.setTime(expirationDate);
                            
                            int paymentYear = cPayment.get(Calendar.YEAR);
                            int expirationYear = cExpiration.get(Calendar.YEAR);
                            
                            if (paymentYear > expirationYear) {
                                this.setStyle("-fx-background-color: #aaaaaa;");
                                return;
                            } else if (paymentYear < expirationYear) {
                                this.setStyle("-fx-background-color:#aaaaff");
                                return;
                            }
                            
                            int paymentMonth = cPayment.get(Calendar.MONTH);
                            int expirationMonth = 
                                    cExpiration.get(Calendar.MONTH);
                            
                            if (paymentMonth > expirationMonth) {
                                this.setStyle("-fx-background-color: #aaaaaa;");
                                return;
                            } else if (paymentMonth < expirationMonth) {
                                this.setStyle("-fx-background-color:#aaaaff");
                                return;
                            }
                            
                            int paymentDay = 
                                    cPayment.get(Calendar.DAY_OF_MONTH);
                            int expirationDay = 
                                    cExpiration.get(Calendar.DAY_OF_MONTH);
                            
                            if (paymentDay > expirationDay) {
                                this.setStyle("-fx-background-color: #aaaaaa;");
                            } else {
                                this.setStyle("-fx-background-color:#aaaaff");
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
                    // I NEED TO BE ABLE:
                    // (1) Read the 'paymentDate' field of the same row.
                    // (2) Change the color of the background of this 
                    // 'expirationDate' cell, in this row.
                    Bill bill = (Bill) t.getTableView()
                                        .getItems()
                                        .get(t.getTablePosition().getRow());

                    bill.setExpirationDate(t.getNewValue());
//
//                    Date paymentDate = bill.getPaymentDate();
//                    long now = new Date().getTime();
//                    now -= now % MILLISECONDS_PER_DAY;
//
//                    long expirationMoment = bill.getExpirationDate().getTime();
//
//                    if (paymentDate == null) {
//                        long daysLeft = (expirationMoment - now) /
//                                         MILLISECONDS_PER_DAY;
//
//                    } else {
//                        long paymentMoment = paymentDate.getTime();
//                        paymentMoment -= paymentMoment % MILLISECONDS_PER_DAY;
//
//                        long days = (expirationMoment - paymentMoment) /
//                                     MILLISECONDS_PER_DAY;
//
//                        System.out.println("Days paid before: " + days); 
//                    }
//                    
//                    System.out.println("Source: " + t.getSource());
//                    System.out.println("Target: " + t.getTarget());
//                    
                }
            }
        );

        tableColumnPaymentDate.setOnEditCommit(
                new EventHandler<CellEditEvent<Bill, Date>>() {

                @Override
                public void handle(CellEditEvent<Bill, Date> t) {
                    ((Bill) t.getTableView()
                             .getItems()
                             .get(t.getTablePosition()
                                   .getRow()))
                             .setPaymentDate(t.getNewValue());
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
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("New file");
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
        editMenuNewBill.setOnAction((e) -> { 
            tableView.getItems().add(new Bill());
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

    private String getCellStyle(long daysLeft) {
        if (daysLeft < 0L) {
            // The bill expires today or expired already. Color red.
            return "-fx-background-color: red; -fx-text-fill: black;" +
                   "-fx-font-weight: bold;";
        } else if (daysLeft > 7L) {
            // "" means clear the table cell style, thus using default colors.
            return "";
        }
        
        int delta = 256 / 8;
        int deltas = (int)(daysLeft + 1) * delta;
        
        int r = 255;
        int g = deltas;
        int b = deltas;
        
        StringBuilder sb = new StringBuilder("-fx-background-color: #");
        sb.append(Integer.toHexString(r));
        sb.append(Integer.toHexString(g));
        sb.append(Integer.toHexString(b));
        sb.append("; -fx-text-fill: black; -fx-font-weight: bold;");
        return sb.toString(); 
    }
}
