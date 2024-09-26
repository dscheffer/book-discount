package com.dscheffer.bookdiscount.dto;

import java.math.BigDecimal;

public record Book(Long id, String name, BigDecimal price) {
}
