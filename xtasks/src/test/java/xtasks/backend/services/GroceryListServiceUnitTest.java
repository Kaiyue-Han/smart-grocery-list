package xtasks.backend.services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.P_item;
import org.mockito.ArgumentCaptor;
import xtasks.backend.repositories.GroceryListRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class GroceryListServiceUnitTest {

    @InjectMocks
    private GroceryListService groceryListService;

    @Mock
    private GroceryListRepository mockgroceryListRepository;

    @Mock
    private PastPurchasesService pastPurchasesService;

    @Test
    public void quantityUpdateShouldSaveTheRequestedValue() {
        L_item item = new L_item();
        item.setQuantity(2);
        when(mockgroceryListRepository.findById(1L)).thenReturn(Optional.of(item));
        groceryListService.updateQuantity(1L, 7);
        assertEquals(7, item.getQuantity());
        verify(mockgroceryListRepository).save(item);
    }

    @Test
    public void invalidQuantitiesShouldNotReachTheRepository() {
        assertThrows(IllegalArgumentException.class, () -> groceryListService.updateQuantity(1L, null));
        assertThrows(IllegalArgumentException.class, () -> groceryListService.updateQuantity(1L, 0));
        assertThrows(IllegalArgumentException.class, () -> groceryListService.updateQuantity(1L, -1));
        verifyNoInteractions(mockgroceryListRepository);
    }

    @Test
    public void missingListItemShouldNotBeSaved() {
        when(mockgroceryListRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> groceryListService.updateQuantity(1L, 7));
        verify(mockgroceryListRepository, never()).save(any());
    }

    @Test
    public void emptyPurchaseShouldNotChangeHistoryOrList() {
        assertTrue(groceryListService.purchaseSelectedItems(Collections.emptySet()).isEmpty());
        verifyNoInteractions(mockgroceryListRepository, pastPurchasesService);
    }

    @Test
    public void purchaseShouldRecordAndDeleteOnlyRetrievedSelectedIds() {
        G_item catalogue = new G_item("Piece", "Fruits", "", "Apple");
        L_item item = new L_item(catalogue, 3);
        item.setId(1L);
        Set<Long> requested = new HashSet<>();
        requested.add(1L);
        requested.add(99L); // A stale selection must not create fictitious history.
        when(mockgroceryListRepository.findAllById(requested)).thenReturn(Collections.singletonList(item));
        Set<Long> purchased = groceryListService.purchaseSelectedItems(requested);
        assertEquals(Collections.singleton(1L), purchased);
        ArgumentCaptor<List<P_item>> history = ArgumentCaptor.forClass(List.class);
        verify(pastPurchasesService).addItemsToPastPurchases(history.capture());
        assertEquals(1, history.getValue().size());
        P_item recorded = history.getValue().get(0);
        assertEquals("Apple", recorded.getName());
        assertEquals("Piece", recorded.getUnit());
        assertEquals("Fruits", recorded.getCategory());
        assertEquals(3, recorded.getQuantity());
        verify(mockgroceryListRepository).deleteByListItemIdIn(Collections.singleton(1L));
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeleteSelectedItems() {
        //Given
        Set<Long> idsToDelete = new HashSet<>();
        idsToDelete.add(1L);
        idsToDelete.add(2L);

        doNothing().when(mockgroceryListRepository).deleteByListItemIdIn(idsToDelete);

        //When
        groceryListService.deleteSelectedItems(idsToDelete);

        //Then
        verify(mockgroceryListRepository, times(1)).deleteByListItemIdIn(idsToDelete);
    }


    @Test
    public void testDeleteAllItems() {
        //Given
        doNothing().when(mockgroceryListRepository).deleteAll();

        //When
        groceryListService.deleteAllItems();

        //Then
        verify(mockgroceryListRepository, times(1)).deleteAll();
    }


    @Test
    public void testCreate() {
        //Given
        L_item item = mock(L_item.class);

        //When
        groceryListService.create(item);

        //Then
        then(mockgroceryListRepository). should().save(item);
    }



    @Test
    public void testGetItembyid() {
        //Given
        Long id = 1L;

        //When
        groceryListService.getItembyid(id);

        //Then
        verify(mockgroceryListRepository, times(1)).findById(id);
    }


    @Test
    public void testDelete() {
        //Given
        L_item item = mock(L_item.class);
        doNothing().when(mockgroceryListRepository).delete(item);

        //When
        groceryListService.delete(item);


        //Then
        verify(mockgroceryListRepository, times(1)).delete(item);
    }


}
