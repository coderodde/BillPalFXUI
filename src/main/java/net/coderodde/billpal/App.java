package net.coderodde.billpal;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {

    private static final int WINDOW_WIDTH  = 640;
    private static final int WINDOW_HEIGHT = 480;
    
    private final TableView tableView = new TableView();
    
    // Table columns:
    private final TableColumn tableColumnAmount;
    private final TableColumn tableColumnDateReceived;
    private final TableColumn tableColumnExpirationDate;
    private final TableColumn tableColumnPaymentDate;
    private final TableColumn tableColumnReceiverIban;
    private final TableColumn tableColumnReferenceNumber;
    private final TableColumn tableColumnBillNumber;
    private final TableColumn tableColumnComment;
    
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
    
    // Other:
    private final BorderPane rootPane = new BorderPane();
    private final Scene scene = new Scene(rootPane);
    
    
    public App() {
        this.tableColumnAmount          = new TableColumn("Amount");
        this.tableColumnDateReceived    = new TableColumn("Date received");
        this.tableColumnExpirationDate  = new TableColumn("Expires");
        this.tableColumnPaymentDate     = new TableColumn("Paid");
        this.tableColumnReceiverIban    = new TableColumn("IBAN");
        this.tableColumnReferenceNumber = new TableColumn("Reference");
        this.tableColumnBillNumber      = new TableColumn("Bill number");
        this.tableColumnComment         = new TableColumn("Comment");
        
        tableView.getColumns().addAll(tableColumnAmount,
                                      tableColumnDateReceived,
                                      tableColumnExpirationDate,
                                      tableColumnPaymentDate,
                                      tableColumnReceiverIban,
                                      tableColumnReferenceNumber,
                                      tableColumnBillNumber,
                                      tableColumnComment);
    }
    
    @Override
    public void start(Stage stage) {
        rootPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("Title");
        buildMenu();
        rootPane.setTop(menuBar);
        rootPane.setCenter(tableView);
        stage.setScene(scene);
        
        // Build and set the application menu.
//        ((BorderPane) scene.getRoot()).getChildren().add(menuBar);
        
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
}
