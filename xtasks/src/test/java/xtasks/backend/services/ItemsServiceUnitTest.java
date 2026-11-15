package xtasks.backend.services;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import xtasks.backend.entities.G_item;
import xtasks.backend.repositories.ItemsRepository;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemsServiceUnitTest {

    @Spy
    public ItemsService itemsService;

    @Test
    public void createShouldFailIfNameIsNull() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, null, null, null);
                }
        );
    }

    @Test
    public void createShouldFailIfNameIsEmpty() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, "", null, null);
                }
        );
    }

    @Test
    public void createShouldFailIfNameIsBlank() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, "                ", null, null);
                }
        );
    }

    @Test
    public void createShouldFailIfImageIsNull() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, null, null, null);
                }
        );
    }

    @Test
    public void createShouldFailIfCategoryIsNull() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, null, null, null);
                }
        );
    }

    @Test
    public void createShouldFailIfCategoryIsNotSelected() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, null, null, "Category");
                }
        );
    }

    @Test
    public void createShouldFailIfUnitIsNull() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create(null, null, null, null);
                }
        );
    }

    @Test
    public void createShouldFailIfUnitIsNotSelected() {
        Assertions.assertThrows(
                GenericServiceException.class,
                () -> {
                    itemsService.create("Unit", null, null, null);
                }
        );
    }


}
