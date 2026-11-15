package xtasks.javafx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xtasks.backend.entities.G_item;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.io.File;

@Component
public class AddItemModalViewController {

    @Autowired
    private GroceryCatalogViewController groceryCatalogViewController;

    @Autowired
    private GroceryListViewController groceryListViewController;

    @Autowired
    private ItemsService itemsService;

    @FXML
    private MenuButton unitBox;

    @FXML
    private MenuButton categoryBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private Text textMessage;

    private String selectedImagePath;

    @FXML
    private Button modalButton;

    private G_item groceryItemToBeEdited;

    private Stage modalStage;

    private void setModalFields(G_item groceryItem) {
        categoryBox.setText(groceryItem.getCategory());
        unitBox.setText(groceryItem.getUnit());
        nameTextField.setText(groceryItem.getName());
        selectedImagePath = groceryItem.getImage();
    }

    public void showModal(ActionEvent event, Parent root, G_item groceryItem) {
        if (groceryItem != null) { // Edit the item.
            setModalFields(groceryItem);
            modalButton.setText("Edit grocery item");
        }

        groceryItemToBeEdited = groceryItem;

        modalStage = new Stage();
        modalStage.setTitle(groceryItem == null ? "Add new grocery item. " : "Edit grocery item.");
        modalStage.setScene(new Scene(root));
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        modalStage.show();
    }

    public void handleSelectionUnit(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();
        unitBox.setText(clickedItem.getText());
    }

    @FXML
    public void modalButtonAction() throws GenericServiceException {
        if (groceryItemToBeEdited == null) { // If it is null we create a new grocery items
            try {
                itemsService.create(unitBox.getText(), nameTextField.getText(), selectedImagePath, categoryBox.getText());
                showMessage("New item added to grocery catalog. ", false);
            } catch (GenericServiceException e) {
                showMessage(e.getMessage(), true);
            }
        } else {
            try {
                itemsService.updateItem(groceryItemToBeEdited.getId(), unitBox.getText(), nameTextField.getText(), selectedImagePath, categoryBox.getText());
                showMessage("Item edited. ", false);
            } catch (GenericServiceException e) {
                showMessage(e.getMessage(), true);
            }
        }
        groceryListViewController.reloadData();
        groceryCatalogViewController.reloadData();
        selectedImagePath = "";
    }

    public void handleSelectionCategory(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();
        categoryBox.setText(clickedItem.getText());
    }

    public void showMessage(String message, boolean isError) {
        if (isError) {
            textMessage.setFill(Color.RED);
        } else {
            textMessage.setFill(Color.GREEN);
        }

        textMessage.setText(message);

        Timeline timeline = new Timeline(new KeyFrame(
                        Duration.seconds(3),
                        ae -> textMessage.setText("")));
        timeline.setCycleCount(1);
        timeline.play();
    }

    @FXML
    private void handleAttachImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();

            System.out.println("Selected image path: " + imagePath);
            selectedImagePath = imagePath;
        }
    }
}
