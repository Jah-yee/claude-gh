package com.storelite.sales.sale;

import java.util.Collection;

public class UnknownSkuException extends RuntimeException {
    public UnknownSkuException(Collection<String> skus) {
        super("Unknown SKU(s): " + String.join(", ", skus));
    }
}
