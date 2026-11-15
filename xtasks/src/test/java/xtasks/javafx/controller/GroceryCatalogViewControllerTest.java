package xtasks.javafx.controller;

import javafx.application.Application;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxToolkit;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.P_item;
import xtasks.backend.repositories.ItemsRepository;
import xtasks.backend.services.ItemsService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.when;


public class GroceryCatalogViewControllerTest {

    GroceryCatalogViewController gccontroller= new GroceryCatalogViewController();
    @Mock
    public ItemsService itemsService;
    @Mock
    private TableView<P_item> tableView;
    @Mock
    public MenuButton category;
    @Mock
    public ItemsRepository itemsRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        tableView=new TableView<>();
        gccontroller.setTableView(tableView);
        gccontroller.category=category;
        gccontroller.itemsService=itemsService;
        gccontroller.selectedCategory="";

        System.out.println("hello iam here");





    }
    @BeforeAll
    public static void initJavaFX() {


        javafx.application.Platform.startup(() -> {
        });
    }
    @Test
    public void testfilteritems(){

       when(itemsService.items()).thenReturn(createDummyGItems());
        System.out.println(itemsService.items());


            gccontroller.filterItems("dummy","Bakery");
            gccontroller.filterItems("","");



    }



    public static List<G_item> createDummyGItems() {
        List<G_item> itemsList = new ArrayList<>();

        G_item identicalitem= new G_item("liter","dairy","/images/standard.jpg","milk");
        itemsList.add(identicalitem);
        return itemsList;
    }
}
