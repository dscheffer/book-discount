package com.dscheffer.bookdiscount.service;

import com.dscheffer.bookdiscount.entity.BookEntity;
import com.dscheffer.bookdiscount.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

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
        var possibleCompositions = combinationSum(IntStream.range(1, bookEntities.size() + 1).boxed().toList(), totalNumberOfBooks);

        var finalPrice = BigDecimal.ZERO;
        for (List<Integer> composition : possibleCompositions) {
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

    private List<List<Integer>> combinationSum(List<Integer> candidates, Integer target) {
        var result = new ArrayList<List<Integer>>();
        combinationSumHelper(candidates, 0, new ArrayList<>(), target, result);
        return result;
    }

    private void combinationSumHelper(
            List<Integer> candidates,
            Integer currentSum,
            List<Integer> currentCombination,
            Integer target,
            List<List<Integer>> result
    ) {
        if (currentSum.equals(target)) {
            result.add(currentCombination);
            return;
        }

        if (currentSum > target) {
            return;
        }

        for (int i = 0; i < candidates.size(); i++) {
            var currentCombinationCopy = new ArrayList<>(currentCombination);
            currentCombinationCopy.add(candidates.get(i));
            combinationSumHelper(candidates, currentSum + candidates.get(i), currentCombinationCopy, target, result);
        }
    }

    private List<Set<BookEntity>> createDiscountSets(
            Map<Long, Integer> bookQuantities,
            List<BookEntity> bookEntities,
            List<Integer> maximumDiscountSetSizes
    ) {
        var bookQuantitiesCopy = new HashMap<>(bookQuantities);
        var discountSets = new ArrayList<Set<BookEntity>>();
        for(Integer maxDiscountSetSize : maximumDiscountSetSizes) {
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
