package xtasks;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.ConfigurableApplicationContext;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class IntegrationTests extends ApplicationTest {

    public FxRobot robot = new FxRobot();
 @Mock
 public FXMLLoader fxmlLoader;
 @Mock
 public  App app;
    public ConfigurableApplicationContext springContext;
    //Javafx Robot will run the app and automatically do certain jobs !
    // watch the magic !
    @Override
    public void start(Stage stage) throws Exception {
        springContext = SpringApplication.run(App.class);  // Ensure Spring context is initialized
        fxmlLoader = new FXMLLoader();  // Direct instantiation
        fxmlLoader.setControllerFactory(springContext::getBean);
        fxmlLoader.setLocation(getClass().getResource("/fxml/mainView.fxml"));
        Parent rootNode = fxmlLoader.load();

        stage.setTitle("SWT Smart Grocery List.");
        stage.setScene(new Scene(rootNode, 800, 600));
        stage.setMaximized(true);
        stage.show();
    }

@Test
@Order(1)
public void testGC(){
        robot.sleep(2500);
    robot.clickOn("Grocery Catalogue");
    String searchQuery = "Blablabla";
    String categoryToSelect = "Bakery";


    robot.clickOn("#searchBar").write(searchQuery);
    robot.clickOn("#category").clickOn(categoryToSelect);

}
@Test
@Order(2)
public void testGL(){
    robot.sleep(2500);
        robot.clickOn("Grocery List");
    String searchQuery = "Blablabla";
    String categoryToSelect = "Bakery";


    robot.clickOn("#searchBar").write(searchQuery);
    robot.clickOn("#category").clickOn(categoryToSelect);
    robot.clickOn("#purchaseItem").clickOn("#removeItem");

}

    @Test
    @Order(3)
    public void testPPL() {

        robot.sleep(2500);

        robot.clickOn("Past Purchases");

        String searchQuery = "Blablabla";
        String categoryToSelect = "Bakery";


       robot.clickOn("#searchBar").write(searchQuery);


        robot.clickOn("#category").clickOn(categoryToSelect);
robot.clickOn("#reset").clickOn("#dsort").clickOn("#dsort1");

          }
}
