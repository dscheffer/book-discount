package com.dscheffer.bookdiscount.service;

import java.math.BigDecimal;
import java.util.Map;

public interface PriceCalculatorService {
    BigDecimal calculatePrice(Map<Long, Integer> booksWithQuantities);
}
