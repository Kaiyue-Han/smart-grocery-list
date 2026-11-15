package xtasks.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.util.Optional;


@Component
public class MainViewController {

  @Autowired
  private ItemsService service;

  @Autowired
  private GroceryCatalogViewController groceryCatalogViewController;

  @Autowired
  private GroceryListViewController groceryListViewController;
  @Autowired
  private AddItemModalViewController addItemModalViewController;

  @Autowired
  private PastPurchasesViewController pastPurchasesViewController;

  @FXML
  private Tab GroceryCatalogView;

  @FXML
  private void initialize() {
    reloadData();
    //Updates suggestions and their display when the GroceryCatalog tab is selected.
    GroceryCatalogView.setOnSelectionChanged(event -> {
      if (GroceryCatalogView.isSelected()) {
        groceryCatalogViewController.updateSuggestions();
        groceryCatalogViewController.updateSuggestionsDisplay();
      }
    });
  }

  public void reloadData() {
    try {
      groceryCatalogViewController.reloadData();
      groceryListViewController.reloadData();
      pastPurchasesViewController.reloadData();
    } catch (GenericServiceException e) {
      handleException(e);
    }
  }

  public void handleExceptionMessage(String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Exception");
    alert.setHeaderText("There has been an error processing your request");
    // Note that the default Alert box might not be big enough to display the whole message
    // TODO adjust size as needed
    alert.setContentText(String.format("Message: %s", message));
    alert.showAndWait();
  }

  public Boolean showAlert(String message) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Alert");
    alert.setContentText(message);

    Optional<ButtonType> result = alert.showAndWait();

    return result.get() == ButtonType.OK;
  }

  public void handleInformationMessage(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Information");
    alert.setHeaderText("Note the following");
    // Note that the default Alert box might not be big enough to display the whole message
    // TODO adjust size as needed
    alert.setContentText(String.format("Message: %s", message));
    alert.showAndWait();
  }

  public void handleException(Exception e) {
    handleExceptionMessage(e.getMessage());
  }
}
