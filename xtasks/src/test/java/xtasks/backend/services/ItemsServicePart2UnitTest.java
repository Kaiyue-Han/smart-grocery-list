package xtasks.backend.services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.L_item;
import xtasks.backend.repositories.GroceryListRepository;
import xtasks.backend.repositories.ItemsRepository;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemsServicePart2UnitTest {

    @InjectMocks
    private ItemsService itemsService;

    @Mock
    private ItemsRepository mockItemsRepository;

    @Mock
    private GroceryListRepository mockGroceryListRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testAddItemToList() {

        G_item gItem = mock(G_item.class);
        List<L_item> emptyList = new ArrayList<>();
        when(gItem.getQuantity()).thenReturn(5);
        when(mockGroceryListRepository.findByCatalogueItem(gItem))
                .thenReturn(emptyList);

        //When
        itemsService.addItemToList(gItem);

        //Then
        verify(mockGroceryListRepository, times(1)).save(any(L_item.class));
    }

    @Test
    public void testAddItemToListWhenNotEmpty() {
        //Given
        G_item gItem = mock(G_item.class);
        L_item existingItem = mock(L_item.class);
        List<L_item> itemList = new ArrayList<>();
        itemList.add(existingItem);

        when(gItem.getQuantity()).thenReturn(1);
        when(mockGroceryListRepository.findByCatalogueItem(gItem)).thenReturn(itemList);

        //When
        itemsService.addItemToList(gItem);

        //Then
        verify(existingItem, times(1)).setQuantity(anyInt());
        verify(mockGroceryListRepository, times(1)).save(existingItem);
    }


    @Test
    public void testDelete() {
        //Given
        G_item item = mock(G_item.class);
        doNothing().when(mockItemsRepository).delete(item);

        //When
        itemsService.delete(item);

        //Then
        verify(mockItemsRepository, times(1)).delete(item);
    }




}
