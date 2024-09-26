package com.dscheffer.bookdiscount.service;

import com.dscheffer.bookdiscount.entity.BookEntity;
import com.dscheffer.bookdiscount.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PriceCalculatorServiceImpl implements PriceCalculatorService {

    private final BookRepository bookRepository;

    @Override
    public BigDecimal calculatePrice(Map<Long, Integer> bookQuantities) {
        var bookEntities = bookRepository.findAllById(bookQuantities.keySet());
        if (bookEntities.size() != bookQuantities.keySet().size()) {
            throw new IllegalArgumentException("Unknown ID used!");
        }

        var totalNumberOfBooks = bookQuantities.values().stream().reduce(0, Integer::sum);
        var possibleCompositions = getIntegerCompositions(totalNumberOfBooks, bookEntities.size());

        var finalPrice = BigDecimal.ZERO;
        for (int[] composition : possibleCompositions) {
            var discountSets = createDiscountSets(bookQuantities, bookEntities, composition);
            var price = calculateDiscountSetsPrice(discountSets);
            if (priceIsSmallerOrFinalPriceIsNotInitialized(finalPrice, price)) {
                finalPrice = price;
            }
        }

        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean priceIsSmallerOrFinalPriceIsNotInitialized(
            BigDecimal finalPrice, BigDecimal price
    ) {
        return finalPrice.compareTo(BigDecimal.ZERO) == 0 || price.compareTo(finalPrice) < 0;
    }

    // See: https://stackoverflow.com/a/67306387
    int[][] getIntegerCompositions(int n, int maximumNumber) {
        return IntStream.range(0, n)
                // prepare 2D arrays of summands
                .mapToObj(i -> IntStream.rangeClosed(1, n - i)
                        .mapToObj(j -> new int[]{j})
                        // Stream<int[][]>
                        .toArray(int[][]::new))
                // sequential summation of array pairs, i.e. getting the
                // cartesian product of arrays, up to the given number
                .reduce((arr1, arr2) -> Arrays.stream(arr1)
                        // combinations of inner arrays
                        .flatMap(row1 -> {
                            // sum of the elements of the first row
                            int sum = Arrays.stream(row1).sum();
                            // if the specified number is reached
                            if (sum == n) return Arrays.stream(new int[][]{row1});
                            // otherwise continue appending summands
                            return Arrays.stream(arr2)
                                    // drop those combinations that are greater
                                    .filter(row2 -> Arrays.stream(row2).sum() + sum <= n)
                                    .map(row2 -> Stream.of(row1, row2)
                                            .flatMapToInt(Arrays::stream).toArray());
                        }) // array of combinations
                        .filter(row2 -> Arrays.stream(row2).noneMatch(i -> i > maximumNumber))
                        .toArray(int[][]::new))
                // otherwise an empty 2D array
                .orElse(new int[0][]);
    }

    private List<Set<BookEntity>> createDiscountSets(
            Map<Long, Integer> bookQuantities,
            List<BookEntity> bookEntities,
            int[] maximumDiscountSetSizes
    ) {
        var bookQuantitiesCopy = new HashMap<>(bookQuantities);
        var discountSets = new ArrayList<Set<BookEntity>>();
        for(int maxDiscountSetSize : maximumDiscountSetSizes) {
            var discountSet = new HashSet<BookEntity>();
            for (BookEntity book : bookEntities) {
                var quantity = bookQuantitiesCopy.get(book.getId());
                if (quantity > 0) {
                    discountSet.add(book);
                    bookQuantitiesCopy.put(book.getId(), quantity - 1);
                }
                if (discountSet.size() == maxDiscountSetSize) {
                    break;
                }
            }
            discountSets.add(discountSet);
        }

        while(hasQuantitiesLeft(bookQuantitiesCopy)) {
            for (BookEntity book : bookEntities) {
                var quantity = bookQuantitiesCopy.get(book.getId());
                if (quantity > 0) {
                    var discountSet = new HashSet<BookEntity>();
                    discountSet.add(book);
                    discountSets.add(discountSet);
                    bookQuantitiesCopy.put(book.getId(), quantity - 1);
                }
            }
        }
        return discountSets;
    }

    private static boolean hasQuantitiesLeft(HashMap<Long, Integer> bookQuantitiesCopy) {
        return bookQuantitiesCopy.entrySet().stream()
                .anyMatch(e -> e.getValue() > 0);
    }

    private BigDecimal discountFactor(Set<BookEntity> discountSet) {
        return switch(discountSet.size()) {
            case 2 -> new BigDecimal("0.95");
            case 3 -> new BigDecimal("0.90");
            case 4 -> new BigDecimal("0.80");
            case 5 -> new BigDecimal("0.75");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal calculateDiscountSetsPrice(List<Set<BookEntity>> discountSets) {
        return discountSets.stream()
                .map(s -> s.stream()
                    .map(BookEntity::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .multiply(discountFactor(s))
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
