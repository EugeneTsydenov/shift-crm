package org.example.model.projection;

import java.math.BigDecimal;

public interface SellerTotalProjection {
    Long getId();

    String getName();

    BigDecimal getTotalAmount();
}