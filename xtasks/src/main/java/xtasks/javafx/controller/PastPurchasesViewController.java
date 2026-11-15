package xtasks.javafx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import xtasks.backend.entities.P_item;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.PastPurchasesService;
import xtasks.backend.services.exceptions.GenericServiceException;


import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;




@Component
public class PastPurchasesViewController {

    @Autowired
    public PastPurchasesService purchaseService;
    @Autowired
    public ItemsService itemsService;
    @Autowired
    private xtasks.backend.services.PurchaseImportService importService;

    @Autowired
    public MainViewController mainViewController;



    @FXML
    public TableView tableView;
    public void setTableView(TableView tableView) {
        this.tableView = tableView;
    }
    @FXML
    private Button dsort;
    @FXML
    private TableColumn<P_item, String> nameColumn;
    @FXML
    private TableColumn<P_item, String> unitColumn;
    @FXML
    private TableColumn<P_item, String> categoryColumn;
    @FXML
    private TableColumn<P_item, Number> quantityColumn;
    @FXML
    private TableColumn<P_item, String> addDateColumn;
    @FXML
    private TableColumn<P_item, Void> deleteColumn;

    public void setSearchBar(TextField searchBar) {
        this.searchBar = searchBar;
    }

    @FXML
    private TextField searchBar;
    @FXML
    private Button Import;

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    private String selectedCategory="";

    @FXML
    private DatePicker datefrom;
    @FXML
    private DatePicker dto;

    @FXML Button reset;

    @FXML
    private Text delete;

    public void setCategory(MenuButton category) {
        this.category = category;
    }

    @FXML
    public MenuButton category;



    private String currentSearchQuery = "";
    private boolean resettingFilters;

    public void filterItems(String searchQuery, String categoryFilter) {
        currentSearchQuery = searchQuery == null ? "" : searchQuery;
        selectedCategory = categoryFilter == null ? "" : categoryFilter;
        String query = currentSearchQuery.toLowerCase(java.util.Locale.ROOT);
        java.time.LocalDate from = datefrom == null ? null : datefrom.getValue();
        java.time.LocalDate to = dto == null ? null : dto.getValue();
        List<P_item> items = purchaseService.P_items().stream()
                .filter(item -> query.isEmpty() || item.getName().toLowerCase(java.util.Locale.ROOT).contains(query)
                        || item.getCategory().toLowerCase(java.util.Locale.ROOT).contains(query))
                .filter(item -> selectedCategory.isEmpty() || "All".equals(selectedCategory)
                        || item.getCategory().equalsIgnoreCase(selectedCategory))
                .filter(item -> from == null || !item.getAddDate().isBefore(from))
                .filter(item -> to == null || !item.getAddDate().isAfter(to))
                .collect(Collectors.toList());
        category.setText(selectedCategory.isEmpty() ? "Category" : selectedCategory);
        tableView.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    public void resetFilters() {
        resettingFilters = true;
        try {
            datefrom.setValue(null);
            dto.setValue(null);
            searchBar.clear();
        } finally {
            resettingFilters = false;
        }
        filterItems("", "");
    }

    @FXML
    public void initialize() {
        Import.setTooltip(new Tooltip("CSV only: name;date;quantity;unit;category (UTF-8, date yyyy-MM-dd)"));
        //-----Responsiveness-----------------------------------------------------------------------

        TableColumn[] pplColumns = {addDateColumn, nameColumn, unitColumn, categoryColumn, quantityColumn, deleteColumn};
        for (TableColumn column : pplColumns) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(pplColumns.length + 0.05));
        }

        //-----------------------------------------------------------------------------------------

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()));
        addDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getAddDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        ));
        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    try {
                        P_item item = getTableView().getItems().get(getIndex());

                        boolean confirmationResult = mainViewController.showAlert("Are you sure you want to delete this item? \n" + item.getName() + ", " + item.getCategory() + ", " + item.getUnit());

                        if (!confirmationResult) {
                            return;
                        }

                        purchaseService.delete(item);
                        delete.setText("Item deleted successfully!");
                        Timeline timeline = new Timeline(new KeyFrame(
                                Duration.seconds(3),
                                ae -> delete.setText("")));
                        timeline.setCycleCount(1);
                        timeline.play();
                        reloadData();
                    } catch (GenericServiceException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });



        for (MenuItem menuItem : category.getItems()) {
            menuItem.setOnAction(event -> {
                filterItems(currentSearchQuery, menuItem.getText());
            });
        }

        datefrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!resettingFilters) filterItems(currentSearchQuery, selectedCategory);
        });
        dto.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!resettingFilters) filterItems(currentSearchQuery, selectedCategory);
        });

        tableView.setEditable(true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!resettingFilters) filterItems(newValue,selectedCategory);
        });

    }

    public void reloadData() throws GenericServiceException {
        filterItems(currentSearchQuery, selectedCategory);

    }
    public void triggersort(){List<P_item> allitems = tableView.getItems();
        try {
            datesort(allitems);
        } catch (GenericServiceException e) {
            System.out.println("problem sorting");
        }
    }
    public void datesort(List<P_item> allitems) throws GenericServiceException {


System.out.println("test sorting date");
        allitems=allitems.stream()
                .sorted(Comparator.comparing(P_item::getAddDate))
                .collect(Collectors.toList());
        List<P_item> finalAllitems = allitems;
        // same story here...
        Platform.runLater(() -> {
            tableView.setItems(FXCollections.observableArrayList(finalAllitems));
        });


    }
    public void triggerreversesort(){
        List<P_item> allitems = tableView.getItems();
        try {
            datesortreverse(allitems);
        } catch (GenericServiceException e) {
            System.out.println("problem with reverse sorting");
        }
    }
    public void datesortreverse(List<P_item> allitems) throws GenericServiceException {


        System.out.println("test sorting date");
        allitems=allitems.stream()
                .sorted(Comparator.comparing(P_item::getAddDate).reversed())
                .collect(Collectors.toList());
        List<P_item> finalAllitems = allitems;
        // same story here...
        Platform.runLater(() -> {
            tableView.setItems(FXCollections.observableArrayList(finalAllitems));
        });


    }
    //---------------------------Function for Imports---------------------
   public void triggerImport(){FileChooser fileChooser = new FileChooser();
       fileChooser.setTitle("Import purchase history (CSV only)");
       fileChooser.getExtensionFilters().addAll(
               new FileChooser.ExtensionFilter("CSV files", "*.csv"));

       File selectedFile = fileChooser.showOpenDialog(null);
        saveImports(selectedFile);
   }
    @FXML
    public void saveImports(File selectedFile) {
        if (selectedFile == null) return;
        try {
            boolean imported = importService.importCsv(selectedFile);
            if (imported) reloadData();
            delete.setText(imported ? "CSV imported successfully."
                    : "Skipped: this file content has already been imported.");
        } catch (IOException | GenericServiceException | RuntimeException error) {
            delete.setText("Import failed: " + error.getMessage());
        }
    }
}
