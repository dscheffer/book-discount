package com.dscheffer.bookdiscount.controller;

import com.dscheffer.bookdiscount.dto.ShoppingCartItem;
import com.dscheffer.bookdiscount.service.PriceCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/price/calculate")
public class PriceCalculatorController {

    private final PriceCalculatorService priceCalculatorService;

    @PostMapping
    public ResponseEntity<BigDecimal> calculateShoppingCardPrice(
            @RequestBody List<ShoppingCartItem> items
    ) {
        var price = priceCalculatorService.calculatePrice(
                items.stream().collect(Collectors.toMap(ShoppingCartItem::getBookId, ShoppingCartItem::getQuantity))
        );
        return ResponseEntity.ok(price);
    }


}
