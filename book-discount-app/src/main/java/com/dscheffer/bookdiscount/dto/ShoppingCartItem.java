package com.dscheffer.bookdiscount.dto;

import lombok.Data;

@Data
public class ShoppingCartItem {
    private Long bookId;
    private Integer quantity;
}
