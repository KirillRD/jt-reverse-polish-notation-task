package com.jt.rpn;

import java.math.BigDecimal;

public interface OperationProcessor {
    String processOperation(BigDecimal x, BigDecimal y);
}
