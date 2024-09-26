package com.dscheffer.bookdiscount.service;


import com.dscheffer.bookdiscount.entity.BookEntity;
import com.dscheffer.bookdiscount.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceCalculatorServiceImplTest {

    PriceCalculatorServiceImpl priceCalculatorService;

    @Mock
    BookRepository bookRepositoryMock;

    @BeforeEach
    void setup() {
        priceCalculatorService = new PriceCalculatorServiceImpl(bookRepositoryMock);
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("provideCalculatePriceData")
    void testCalculatePrice(
            String displayName,
            Map<Long, Integer> bookQuantities,
            List<BookEntity> bookEntities,
            BigDecimal expectedResult
    ) {
        when(bookRepositoryMock.findAllById(bookQuantities.keySet()))
                .thenReturn(bookEntities);

        var actualResult = priceCalculatorService.calculatePrice(bookQuantities);

        assertThat(actualResult).isEqualByComparingTo(expectedResult);
    }


    private static Stream<Arguments> provideCalculatePriceData() {
        return Stream.of(
                Arguments.of(
                        "Buch mit Menge 0 ausgewählt",
                        Map.of(1L, 0),
                        List.of(new BookEntity(1L, "Buch1", new BigDecimal("8.00"))),
                        BigDecimal.ZERO
                ),
                Arguments.of(
                        "Ein einzelnes Buch ohne Rabatt",
                        Map.of(1L, 1),
                        List.of(new BookEntity(1L, "Buch1", new BigDecimal("8.00"))),
                        new BigDecimal("8.00")
                ),
                Arguments.of(
                        "Zwei verschiedene Bücher mit 5% Rabatt",
                        Map.of(1L, 1, 2L, 1),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("15.2")
                ),
                Arguments.of(
                        "Drei Bücher, aber nur zwei verschiedene",
                        Map.of(1L, 1, 2L, 2),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("23.2")
                ),
                Arguments.of(
                        "Drei verschiedene Bücher mit 10% Rabatt",
                        Map.of(1L, 1, 2L, 1, 3L, 1),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("21.6")
                ),
                Arguments.of(
                        "Vier verschiedene Bücher mit 20% Rabatt",
                        Map.of(1L, 1, 2L, 1, 3L, 1, 4L, 1),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("25.6")
                ),
                Arguments.of(
                        "Fünf verschiedene Bücher mit 25% Rabatt",
                        Map.of(1L, 1, 2L, 1, 3L, 1, 4L, 1, 5L, 1),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00")),
                                new BookEntity(5L, "Buch5", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("30")
                ),
                Arguments.of(
                        "Ein einzelnes Buch, aber alle anderen mit Menge 0 ausgewählt",
                        Map.of(1L, 0, 2L, 1, 3L, 0, 4L, 0, 5L, 0),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00")),
                                new BookEntity(5L, "Buch5", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("8")
                ),
                Arguments.of(
                        "Buch1 mal 2; Buch2 mal 2; Buch3 mal 2; Buch4 mal 1; Buch5 mal 1",
                        Map.of(1L, 2, 2L, 2, 3L, 2, 4L, 1, 5L, 1),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00")),
                                new BookEntity(5L, "Buch5", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("51.2")
                ),
                Arguments.of(
                        "Buch1 mal 3; Buch2 mal 3; Buch3 mal 3; Buch4 mal 2; Buch5 mal 2",
                        Map.of(1L, 3, 2L, 3, 3L, 3, 4L, 2, 5L, 2),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00")),
                                new BookEntity(5L, "Buch5", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("81.2")
                ),
                Arguments.of(
                        "Buch1 mal 4; Buch2 mal 4; Buch3 mal 4; Buch4 mal 3; Buch5 mal 3",
                        Map.of(1L, 4, 2L, 4, 3L, 4, 4L, 3, 5L, 3),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00")),
                                new BookEntity(5L, "Buch5", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("111.2")
                ),
                Arguments.of(
                        "Buch1 mal 5; Buch2 mal 5; Buch3 mal 5; Buch4 mal 4; Buch5 mal 4",
                        Map.of(1L, 5, 2L, 5, 3L, 5, 4L, 4, 5L, 4),
                        List.of(
                                new BookEntity(1L, "Buch1", new BigDecimal("8.00")),
                                new BookEntity(2L, "Buch2", new BigDecimal("8.00")),
                                new BookEntity(3L, "Buch3", new BigDecimal("8.00")),
                                new BookEntity(4L, "Buch4", new BigDecimal("8.00")),
                                new BookEntity(5L, "Buch5", new BigDecimal("8.00"))
                        ),
                        new BigDecimal("141.2")
                )
        );
    }
}