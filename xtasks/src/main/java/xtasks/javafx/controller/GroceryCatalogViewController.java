package xtasks.javafx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.S_item;
import xtasks.backend.services.AprioriService;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.SuggestionsService;
import xtasks.backend.services.exceptions.GenericServiceException;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

@Component
public class GroceryCatalogViewController {

    @Autowired
    public ItemsService itemsService;

    @Autowired
    MainViewController mainViewController;

    @Autowired
    private SuggestionsService suggestionService;

    @Autowired
    private AprioriService aprioriService;

    @Autowired
    private AddItemModalViewController addItemModalViewController;

    @Autowired
    private GroceryListViewController groceryListViewController;

    @Autowired
    private ApplicationContext appContext;

    @FXML
    private TableColumn nameColumn;

    @FXML
    private TableColumn categoryColumn;

    @FXML
    private TableColumn unitColumn;

    @FXML
    private TableColumn imageColumn;

    public void setTableView(TableView tableView) {
        this.tableView = tableView;
    }

    @FXML
    public TableView tableView;

    @FXML
    public TextField searchBar;

    public String selectedCategory="";

    @FXML
    public MenuButton category;

    @FXML
    private TableColumn<G_item, Number> quantityColumn;

    @FXML
    private TableColumn<G_item,Void> addToCartColumn;
    @FXML
    private TableColumn<G_item, Void> deleteColumn;

    @FXML
    private Text congrats;
    @FXML
    private Text error;

    @FXML
    private Text deletefeedback;
    @FXML
    private TableColumn<G_item,Void> editItemColumn;

    //----------------------------------------------------------------

    @FXML
    private TableView stableView;

    @FXML
    private Button suggestion;

    @FXML
    private Button close;

    @FXML
    private Button addall;

    @FXML
    private Button rejectall;

    @FXML
    private VBox vbox;

    @FXML
    private TableColumn<S_item, String> snameColumn;
    @FXML
    private TableColumn<S_item, String> scategoryColumn;
    @FXML
    private TableColumn<S_item, Integer> squantityColumn;
    @FXML
    private TableColumn<S_item, String> sunitColumn;
    @FXML
    private TableColumn<S_item,Void> saddColumn;
    @FXML
    private TableColumn<S_item, Void> rejectColumn;
    @FXML
    private TableColumn<S_item, String> reasonColumn;


    // Search functionality will be coded here --------------------
    public void filterItems(String searchQuery, String selectedCategory) {
        List<G_item> allItems = itemsService.items();

        // If searchQuery is not null and not empty, apply the search filter
        if (searchQuery != null && !searchQuery.isEmpty()) {
            allItems = allItems.stream()
                    .filter(item -> item.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            item.getCategory().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // If selectedCategory is not null and not empty, apply the category filter
        if ((selectedCategory != null && !selectedCategory.isEmpty() )&& !(selectedCategory.equals("All"))) {
            allItems = allItems.stream()
                    .filter(item -> item.getCategory().equalsIgnoreCase(selectedCategory))
                    .collect(Collectors.toList());
        }

        tableView.setItems(FXCollections.observableArrayList(allItems));
        category.setText(selectedCategory);
    }

    @FXML
    private void initialize() throws IOException {
////------------------Automated by an AI tool until line-----------
//        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
////--------------------------------------------------------------------

        tableView.setEditable(true);
        quantityColumn.setEditable(true);
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());

        for (MenuItem menuItem : category.getItems()) {
            menuItem.setOnAction(event -> {
                selectedCategory = menuItem.getText();
                if (selectedCategory.equals("All")) {
                    filterItems(null,selectedCategory);
                } else {
                    filterItems(selectedCategory,selectedCategory);
                }
            });
        }

        //-----Responsiveness-----------------------------------------------------------------------

        TableColumn[] gcColumns = {imageColumn, nameColumn, unitColumn, categoryColumn, quantityColumn, addToCartColumn, editItemColumn, deleteColumn};

        for (TableColumn column : gcColumns) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(gcColumns.length + 0.05));
        }

        TableColumn[] suggestionColumns = {snameColumn, sunitColumn, scategoryColumn, squantityColumn, saddColumn, rejectColumn, reasonColumn};

        for (TableColumn column : suggestionColumns) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(suggestionColumns.length + 0.05));
        }

        //-----------------------------------------------------------------------------------------

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterItems(newValue,selectedCategory);
        });

        quantityColumn.setCellFactory(col -> new TableCell<G_item, Number>() {
            private final TextField textField = new TextField();

            {
                textField.setEditable(true);
                textField.setPrefWidth(col.getPrefWidth() - 2);

                textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        try {
                            int newQuantity = Integer.parseInt(textField.getText());
                            commitEdit(newQuantity);
                        } catch (NumberFormatException e) {
                            cancelEdit();
                        }
                    }
                });

                // FIXME: You have to work on the regex. If it is limited to two digits and you write it happens the cell bug.
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    // only number permitted
                    if (!newValue.matches("\\d*")) {
                        textField.setText(newValue.replaceAll("[^\\d]", ""));
                    }

                    // make sure the number will not be 0.
                    if (newValue.matches("0*")) {
                        textField.setText("1");
                    }
                });
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (!isEmpty()) {
                    setGraphic(textField);
                    textField.setText(getString());
                    textField.selectAll();
                    textField.requestFocus();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getString());
            }

            @Override
            public void commitEdit(Number value) {
                super.commitEdit(value);

                G_item item = getTableView().getItems().get(indexProperty().get());
                item.setQuantity(Integer.parseInt(textField.getText()));
            }

            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                if (isEditing()) {
                    textField.setText(getString());
                    setGraphic(textField);
                    return;
                }

                G_item groceryItem = getTableView().getItems().get(indexProperty().get());
                Integer groceryItemQuantity = groceryItem.getQuantity();

                if (groceryItemQuantity.equals(1)) { // First time is equal to 1.
                    groceryItemQuantity = groceryItem.getDefaultQuantity();
                }

                groceryItem.setQuantity(groceryItemQuantity);

                setGraphic(textField);
                textField.setText(groceryItemQuantity.toString());
            }

            private String getString() {
                return getItem() == null ? "" : getItem().toString();
            }
        });

        // source : // source : https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view

        addToCartColumn.setCellFactory(column -> new TableCell<>() {
            private final Button addButton = new Button("Add ");
            {
                addButton.setOnAction(event -> {
                    G_item item = getTableView().getItems().get(getIndex());
                    int quantity = item.getQuantity(); //Quantity should be delivered to Grocery list with the item.
                    System.out.println("to be added to shopping list"+item+quantity);
                    itemsService.addItemToList(item);
                    congrats.setText("Item added successfully to the grocery list!");
                    updateSuggestions(item.getName());
                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.seconds(3),
                            ae -> congrats.setText("")));
                    timeline.setCycleCount(1);
                    timeline.play();
                    try {
                        groceryListViewController.reloadData();
                    } catch (GenericServiceException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addButton);

            }
        });
        imageColumn.setCellFactory(column -> new TableCell<G_item, String>() {
            private final ImageView imageView = new ImageView();
            { imageView.setFitHeight(50); imageView.setPreserveRatio(true); }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (url == null || empty) {
                    setGraphic(null);

                } else {
                    File file = new File(url);
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

        editItemColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit Grocery Item");
            {
                editButton.setOnAction(event -> {
                    G_item itemToBeEdited = getTableView().getItems().get(getIndex());
                    try {
                        editSelectedItem(event, itemToBeEdited);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);

            }
        });

        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    try {
                        G_item item = getTableView().getItems().get(getIndex());
                        boolean confirmationResult = mainViewController.showAlert("Are you sure you want to delete this item? \n" + item.getName() + ", " + item.getCategory() + ", " + item.getUnit());
                        if (!confirmationResult) {
                            return;
                        }

                        itemsService.delete(item);
                        groceryListViewController.reloadData();

                        deletefeedback.setText("Item deleted successfully ! :)");
                        Timeline timeline = new Timeline(new KeyFrame(
                                Duration.seconds(3),
                                ae -> deletefeedback.setText("")));
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


        tableView.setItems(FXCollections.observableList(itemsService.items()));
        System.out.println("result of test : "+ itemsService.items());
//---------------------------------------Drawer--------------------------------------------

        updateSuggestions();
        close.setOnAction(event -> toggleDrawer());

        snameColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getName() : "");
        });

        scategoryColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getCategory() : "");
        });


        sunitColumn.setCellValueFactory(cellData -> {
            G_item gItem = cellData.getValue().getCatalogueItem();
            return new SimpleStringProperty(gItem != null ? gItem.getUnit() : "");
        });


        squantityColumn.setEditable(true);
        squantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        squantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));


        reasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));


        /**
         * Configures the cell factory for the "Add" button in the table column.
         * This method sets up the button behavior to add a suggestion to the grocery list,
         * provide feedback, and update the view accordingly.
         */
        saddColumn.setCellFactory(column -> new TableCell<>() {

            private final Button saddButton = new Button("Add ");
            {
                saddButton.setOnAction(event -> {
                    S_item suggestion = getTableView().getItems().get(getIndex());
                    int quantity = suggestion.getQuantity(); //Quantity should be delivered to Grocery list with the item.
                    System.out.println("to be added to shopping list"+suggestion+quantity);
                    suggestionService.addItemToList(suggestion);
                    congrats.setText("Item added successfully to the grocery list!");
                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.seconds(3),
                            ae -> congrats.setText("")));
                    timeline.setCycleCount(1);
                    timeline.play();
                    try {
                        suggestionService.delete(suggestion);
                        groceryListViewController.reloadData();
                        reloadData();
                    } catch (GenericServiceException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : saddButton);

            }
        });

        /**
         * Configures the cell factory for the "Reject" button in the table column.
         * This method sets up the button behavior to reject a suggestion and update its status.
         */
        rejectColumn.setCellFactory(column -> new TableCell<>() {
            private final Button rejectButton = new Button("Reject");

            {
                rejectButton.setOnAction(event -> {

                    try {S_item suggestion = getTableView().getItems().get(getIndex());

                        suggestion.setIsReject(true);
                        suggestionService.updateSItem(suggestion);
                        Timeline timeline = new Timeline(new KeyFrame(
                                Duration.seconds(3),
                                ae -> deletefeedback.setText("")));
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
                setGraphic(empty ? null : rejectButton);
            }
        });

        /**
         * Configures the cell factory for the quantity column in the table.
         * This method sets up the behavior to edit and update the quantity of a suggestion.
         */
        squantityColumn.setCellFactory(col -> new TableCell<S_item, Integer>() {
            private final TextField stextField = new TextField();

            {
                stextField.setEditable(true);
                stextField.setPrefWidth(col.getPrefWidth() - 2);
                stextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        try {
                            int newQuantity = Integer.parseInt(stextField.getText());
                            commitEdit(newQuantity);
                        } catch (NumberFormatException e) {
                            cancelEdit();
                        }
                    }
                });
                stextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    // only number permitted
                    if (!newValue.matches("\\d*")) {
                        stextField.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                    // make sure the number will not be 0.
                    if (newValue.matches("0*")) {
                        stextField.setText("1");
                    }
                });
            }
            @Override
            public void startEdit() {
                super.startEdit();
                if (!isEmpty()) {
                    setGraphic(stextField);
                    stextField.setText(getString());
                    stextField.selectAll();
                    stextField.requestFocus();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                S_item suggestion = getTableView().getItems().get(getIndex());
                setText(getString());
            }


            @Override
            public void commitEdit(Integer value) {
                super.commitEdit(value);
                S_item suggestion = getTableView().getItems().get(getIndex());
                int newQuantity;
                try {
                    newQuantity = Integer.parseInt(stextField.getText());
                } catch (NumberFormatException e) {
                    Integer currentItem = getItem();
                    newQuantity = currentItem != null ? currentItem.intValue() : 1;
                }
                System.out.println("to be added to shopping list"+ newQuantity);
                suggestion.setQuantity(newQuantity);
                suggestionService.updateSItem(suggestion);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);

                } else {
                    if (isEditing()) {
                        if(stextField != null) {
                            stextField.setText(getString());
                        }
                        setGraphic(stextField);

                    } else {
                        setGraphic(stextField);
                        stextField.setText(getString());
                    }
                }
            }

            private String getString() {
                return getItem() == null ? "" : getItem().toString();
            }

        });
        stableView.setEditable(true);
        stableView.setItems(FXCollections.observableList(suggestionService.suggestions()));
    }
    /**
     * Toggles the display state of the drawer.
     * This method uses a translation animation to toggle the visibility of the drawer.
     */
    private void toggleDrawer() {
        double drawerHeight = 297;
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), vbox);
        System.out.println("---------test1111");
        if (vbox == null) { System.out.println("vbox is null!"); return; }
        if (vbox.getTranslateY() < 0) {
            transition.setToY(0);
            //System.out.println("---------test1111");
        } else {
            transition.setToY(-drawerHeight);
        }
        transition.play();
    }

//    @PostConstruct
//    public void init() {
//        Map<String, Long> recommendations = suggestionService.generateRecommendations();
//        suggestionService.createSItems(recommendations);
//    }

    //editing Item section -- Andrian
    private Parent getRoot() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/addItemModalView.fxml"));
        fxmlLoader.setControllerFactory(appContext::getBean);

        return (Parent) fxmlLoader.load();
    }
    public void editSelectedItem(ActionEvent event, G_item item) throws IOException {
        Parent root = getRoot();

        addItemModalViewController.showModal(event, root, item);
    };

    //------------------------------

    @FXML
    public void addButtonAction(ActionEvent event) throws IOException {
        // Showing the modal.
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/addItemModalView.fxml"));
        fxmlLoader.setControllerFactory(appContext::getBean);

        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();

        stage.setTitle("Add new item");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) event.getSource()).getScene().getWindow());
        stage.show();
    }
//    public void loadData() {
//        List<S_item> allItems = suggestionService.suggestions();
//        List<S_item> filteredItems = allItems.stream()
//                .filter(s -> !s.getIsReject())
//                .collect(Collectors.toList());
//
//        ObservableList<S_item> observableList = FXCollections.observableArrayList(filteredItems);
//        stableView.setItems(observableList);
//    }
    @FXML
    /**
     * Adds all non-rejected suggestions to the grocery list, marks them as rejected,
     * and updates the view accordingly.
     */
    public void addallAction(ActionEvent event) throws IOException {
        List<S_item> suggestions = suggestionService.suggestions();

        for (S_item suggestion : suggestions) {
            if (!suggestion.getIsReject()) {
                suggestionService.addItemToList(suggestion);
                suggestion.setIsReject(true);
                suggestionService.updateSItem(suggestion);
            }
        }
        try {
            groceryListViewController.reloadData();
            reloadData();
        } catch (GenericServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Rejects all suggestions, marks them as rejected, and updates the view accordingly.
     */
    @FXML
    public void rejectallAction(ActionEvent event) throws IOException {
        System.out.println("rrraction");
        List<S_item> suggestions = suggestionService.suggestions();
        for (S_item suggestion : suggestions) {
            suggestion.setIsReject(true);
            suggestionService.updateSItem(suggestion);
        }
        try {
            groceryListViewController.reloadData();
            reloadData();
        } catch (GenericServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the suggestion action, which toggles the drawer, resets the new suggestions count,
     * and updates the suggestions display in the view.
     */
    @FXML
    public void suggestionAction(ActionEvent event) throws IOException {
        toggleDrawer();
        System.out.println("addaction");
        suggestionService.resetNewSuggestionsCount();
        updateSuggestionsDisplay();
        //updateSuggestions();
    }

    /**
     * Updates the suggestions display text in the view based on the count of new suggestions.
     */
    public void updateSuggestionsDisplay() {
        int count = suggestionService.getNewSuggestionsCount();
        String displayText = count > 0 ? " New Suggestions: " + count : "Suggestions";
        suggestion.setText(displayText);
    }

    /**
     * Updates the suggestions by generating new recommendations, creating suggestion items,
     * and refreshing the view.
     * This method is intended to update suggestions for regularly purchased items and
     * should be called when clicking on the GroceryCatalog tab.
     */
    public void updateSuggestions(){
        Map<String, Long> recommendations = suggestionService.generateRecommendations();
        suggestionService.createSItems(recommendations);
        try {
            reloadData();
        } catch (GenericServiceException e) {
            throw new RuntimeException(e);
        }
        updateSuggestionsDisplay();
    }

    /**
     * Updates the suggestions for related items based on the provided item.
     * This method generates recommendations for items related to the given item
     * and creates suggestion items accordingly. It should be called when clicking
     * the "Add" button for each item in the catalog.
     *
     * @param item The item to which related suggestions should be associated.
     *             It represents the item being added.
     */
    public void updateSuggestions(String item){
        Set<String> relatedItems;
        relatedItems = aprioriService.generateRecommendations(item);
        for (String relatedItem : relatedItems) {
            suggestionService.createSItems(relatedItem);
        }

        try {
            reloadData();
        } catch (GenericServiceException e) {
            throw new RuntimeException(e);
        }
        updateSuggestionsDisplay();
    }

    /**
     * Refreshes the view by loading the latest items and suggestions from their respective services.
     */
    public void reloadData() throws GenericServiceException {
        List<G_item> items = itemsService.items();
        List<S_item> suggestions = suggestionService.suggestions();

        tableView.getItems().clear();
        for (G_item item : items) {
            tableView.getItems().add(item);
        }

        stableView.getItems().clear();
        for (S_item suggestion : suggestions) {
            if (!suggestion.getIsReject()) {
                stableView.getItems().add(suggestion);
            }
        }

    }
}
