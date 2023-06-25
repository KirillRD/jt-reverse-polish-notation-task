package com.jt.rpn;

import java.util.Deque;
import java.util.List;

public interface TokenProcessor {
    void processToken(List<String> rpn, Deque<String> stack, String token);
}
