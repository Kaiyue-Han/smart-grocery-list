package xtasks.javafx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.context.ApplicationContext;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import xtasks.backend.entities.G_item;
import xtasks.backend.entities.L_item;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.GroceryListService;
import xtasks.backend.services.exceptions.GenericServiceException;

import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
public class GroceryListViewController {

    @Autowired
    private ItemsService itemsService;
    @Autowired
    private GroceryListService listService;

    @Autowired
    private MainViewController mainViewController;

    @Autowired
    private GroceryCatalogViewController groceryCatalogViewController;


    @Autowired
    private ApplicationContext appContext;

    private Set<Long> selectedItemsIds = new HashSet<>();


    @FXML
    private TableView<L_item> tableView;


    @FXML
    private TableColumn<L_item, String> imageColumn;

    @FXML
    private TableColumn<L_item, String> nameColumn;

    @FXML
    private TableColumn<L_item, String> categoryColumn;

    @FXML
    private TableColumn<L_item, String> unitColumn;

    @FXML
    private TableColumn<L_item, Integer> quantityColumn;
    @FXML
    private Text success;

    @FXML
    private TableColumn<L_item, Boolean> checkboxColumn;
    @FXML
    private TableColumn<L_item, Void> deleteColumn;
    @FXML
    private MenuButton category;
    @FXML
    private TextField searchBar;
    @FXML
    private Text delete;
    private String selectedCategory="";


    @FXML
    private Button purchaseItem;
    @FXML
    private Button removeItem;

    private void filterItems(String searchQuery, String selectedCategory) {
        List<L_item> allItems = listService.L_items();

        // If searchQuery is not null and not empty, apply the search filter
        if (searchQuery != null && !searchQuery.isEmpty()) {
            allItems = allItems.stream()
                    .filter(item -> item.getCatalogueItem().getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            item.getCatalogueItem().getCategory().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // If selectedCategory is not null and not empty, apply the category filter
        if ((selectedCategory != null && !selectedCategory.isEmpty() )&& !(selectedCategory.equals("All"))) {
            allItems = allItems.stream()
                    .filter(item -> item.getCatalogueItem().getCategory().equalsIgnoreCase(selectedCategory))
                    .collect(Collectors.toList());
        }
        category.setText(selectedCategory);
        tableView.setItems(FXCollections.observableArrayList(allItems));
    }
    @FXML
    public void initialize() throws IOException {
        //-----Responsiveness-----------------------------------------------------------------------

        TableColumn[] glColumns = {imageColumn, nameColumn, unitColumn, categoryColumn, quantityColumn, checkboxColumn, deleteColumn};
        for (TableColumn column : glColumns) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(glColumns.length + 0.05));
        }

        //-----------------------------------------------------------------------------------------

        for (MenuItem menuItem : category.getItems()) {
            menuItem.setOnAction(event -> {
                selectedCategory = menuItem.getText();
                if (selectedCategory.equals("All")) {
                    filterItems(null,selectedCategory);
                }else{
                    filterItems(selectedCategory,selectedCategory);}
            });
        }
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterItems(newValue,selectedCategory);
        });

        imageColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getImage() : "");
        });

        nameColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getName() : "");
        });

        categoryColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getCategory() : "");
        });

        unitColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getUnit() : "");
        });
        tableView.setEditable(true);
        quantityColumn.setEditable(true);




        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter() {
            @Override
            public Integer fromString(String value) {
                try {
                    return super.fromString(value);
                } catch (NumberFormatException invalidQuantity) {
                    return null;
                }
            }
        }));
        quantityColumn.setOnEditCommit(event -> {
            L_item currentItem = event.getRowValue();
            int previousQuantity = currentItem.getQuantity();
            try {
                listService.updateQuantity(currentItem.getId(), event.getNewValue());
                currentItem.setQuantity(event.getNewValue());
            } catch (RuntimeException failure) {
                currentItem.setQuantity(previousQuantity);
                mainViewController.handleException(failure);
            } finally {
                tableView.refresh();
            }
        });

        checkboxColumn.setCellFactory(col -> new TableCell<L_item, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    checkBox.setSelected(false);
                    setGraphic(null);
                } else {
                    L_item row = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(selectedItemsIds.contains(row.getId()));
                    setGraphic(checkBox);
                    checkBox.setOnAction(e -> {
                        L_item currentItem = getTableView().getItems().get(getIndex());
                        if (checkBox.isSelected()) {
                            selectedItemsIds.add(currentItem.getId());
                        } else {
                            selectedItemsIds.remove(currentItem.getId());
                        }
                    });
                }
            }
        });

        imageColumn.setCellFactory(column -> new TableCell<L_item, String>() {
            private final ImageView imageView = new ImageView();
            { imageView.setFitHeight(50); imageView.setPreserveRatio(true); }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (url == null || empty) {
                    setGraphic(null);

                } else {
                    File file =new File(url);
                    String imageURL = null;
                    try {
                        imageURL = file.toURI().toURL().toExternalForm();
                        Image image = new Image(imageURL, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);

                    }
                }
            }
        });

        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    try {
                        L_item groceryListItem = getTableView().getItems().get(getIndex());
                        G_item groceryItem = groceryListItem.getCatalogueItem();

                        boolean confirmationResult = mainViewController.showAlert("Are you sure you want to delete this item? \n" + groceryItem.getName() + ", " + groceryItem.getCategory() + ", " + groceryItem.getUnit());

                        if (!confirmationResult) {
                            return;
                        }


                        listService.delete(groceryListItem);
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

        removeItem.setOnAction(this::removeAllItems);
        purchaseItem.setOnAction(this::addToPastPurchaseList);
        tableView.setEditable(true);

    }

    @FXML
    private void removeAllItems(ActionEvent event) {

        if (tableView.getItems().isEmpty()) {
            delete.setFill(Color.BLUE);
            delete.setText("There are no items to be deleted. ");
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(3),
                    ae -> delete.setText("")));
            timeline.setCycleCount(1);
            timeline.play();

            return;
        }

        boolean confirmationResult = mainViewController.showAlert("Are you sure to delete all the items from the grocery list ? ");

        if (!confirmationResult) {
            return;
        }

        tableView.getItems().clear();
        tableView.refresh();

        listService.deleteAllItems();

        delete.setText("All items deleted successfully!");
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(3),
                ae -> delete.setText("")));
        timeline.setCycleCount(1);
        timeline.play();
    }

    @FXML
    private void addToPastPurchaseList(ActionEvent event){

        Set<Long> visibleSelectedIds = tableView.getItems().stream()
                .filter(item -> selectedItemsIds.contains(item.getId()))
                .map(L_item::getId).collect(Collectors.toSet());
        if (visibleSelectedIds.isEmpty()) {
            success.setText("Select a visible item to purchase.");
            return;
        }
        Set<Long> purchasedIds;
        try {
            purchasedIds = listService.purchaseSelectedItems(visibleSelectedIds);
        } catch (RuntimeException failure) {
            mainViewController.handleException(failure);
            return;
        }
        if (purchasedIds.isEmpty()) {
            success.setText("The selected items are no longer in the shopping list.");
            mainViewController.reloadData();
            return;
        }
        selectedItemsIds.removeAll(purchasedIds);
        success.setText("Item added to past purchase list!");
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(3),
                ae -> success.setText("")));
        timeline.setCycleCount(1);
        timeline.play();


//        groceryCatalogViewController.updateSuggestions();
//        groceryCatalogViewController.updateSuggestionsDisplay();

        tableView.refresh();
        mainViewController.reloadData();
    }

    public void reloadData() throws GenericServiceException {
        List<L_item> items = listService.L_items();
        selectedItemsIds.retainAll(items.stream().map(L_item::getId).collect(Collectors.toSet()));

        imageColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getImage() : "");
        });

        nameColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getName() : "");
        });

        categoryColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getCategory() : "");
        });

        unitColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getUnit() : "");
        });

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        filterItems(searchBar.getText(), selectedCategory);

    }
}


