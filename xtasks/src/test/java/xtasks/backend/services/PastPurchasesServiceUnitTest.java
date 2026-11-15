package xtasks.backend.services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.P_item;
import xtasks.backend.repositories.GroceryListRepository;
import xtasks.backend.repositories.PastPurchasesListRepository;
import xtasks.backend.repositories.SuggestionRepository;

import java.util.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class PastPurchasesServiceUnitTest {

    @InjectMocks
    private PastPurchasesService pastPurchasesService;

    @Mock
    private PastPurchasesListRepository mockPastPurchasesListRepository;

    @Mock
    private SuggestionRepository mockSuggestionRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddItemsToPastPurchases() {
        //Given
        P_item newItem = new P_item("Apple", "Piece", "Fruits", 2, LocalDate.of(2026, 11, 15));
        List<P_item> items = Collections.singletonList(newItem);

        when(mockPastPurchasesListRepository.findByAddDateAndNameAndUnitAndCategory(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        when(mockPastPurchasesListRepository.save(any(P_item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        pastPurchasesService.addItemsToPastPurchases(items);

        //Then
        verify(mockPastPurchasesListRepository, times(items.size())).save(any(P_item.class));
        verify(mockPastPurchasesListRepository).findByAddDateAndNameAndUnitAndCategory(
                newItem.getAddDate(), "Apple", "Piece", "Fruits");

        verify(mockSuggestionRepository).deleteAllByIsReject(true);

    }

    @Test
    public void testAddItemsToPastPurchasesWhenItemExists() {
        // Given
        P_item newItem = new P_item("Apple", "Piece", "Fruits", 2, LocalDate.of(2026, 11, 15));
        List<P_item> items = Collections.singletonList(newItem);
        P_item existingItem = new P_item("Apple", "Piece", "Fruits", 3, newItem.getAddDate());
        Optional<P_item> existingItemOpt = Optional.of(existingItem);

        when(mockPastPurchasesListRepository.findByAddDateAndNameAndUnitAndCategory(any(), any(), any(), any()))
                .thenReturn(existingItemOpt);

        // When
        pastPurchasesService.addItemsToPastPurchases(items);

        // Then
        verify(mockPastPurchasesListRepository).save(existingItem);
        assertEquals(5, existingItem.getQuantity());
        verify(mockSuggestionRepository).deleteAllByIsReject(true);
    }

}





