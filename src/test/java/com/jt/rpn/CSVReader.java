package com.jt.rpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVReader {
    private static final String DELIMITER = ";";
    public List<List<String>> getData(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName)))) {
            AtomicInteger index = new AtomicInteger(0);
            return br.lines().map(line -> Stream.concat(
                    Stream.of(line.split(DELIMITER)), Stream.of(String.valueOf(index.addAndGet(1)))
            ).collect(Collectors.toList())).collect(Collectors.toList());
        }
    }
}
