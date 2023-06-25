package com.jt.rpn;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ReversePolishNotationTest {
    private static final String WHITESPACE_REGEX = "\\s";
    private static final String GET_RPN = "getRPN";
    private static final String EVAL_RPN = "evalRPN";
    private static final String TEST_DATA_FILE_NAME = "RPN_test_data.csv";

    private List<List<String>> getData() throws IOException {
        CSVReader csvReader = new CSVReader();
        return csvReader.getData(TEST_DATA_FILE_NAME);
    }

    private void runGetRPNTest(String actualExpression, String expected, int rowNumber) {
        ReversePolishNotation rpn = new ReversePolishNotation();
        try {
            assertGetRPN(rpn.get(actualExpression), expected, rowNumber);
        } catch (Exception e) {
            assertException(e, expected, GET_RPN, rowNumber);
        }
    }

    private void runEvalRPNTest(String actualExpression, String expected, int rowNumber) {
        ReversePolishNotation rpn = new ReversePolishNotation();
        try {
            assertEvalRPN(rpn.eval(actualExpression), expected, rowNumber);
        } catch (Exception e) {
            assertException(e, expected, EVAL_RPN, rowNumber);
        }
    }

    private void assertGetRPN(List<String> actual, String expected, int rowNumber) {
        assertResult(removeWhitespace(actual.toString()), removeWhitespace(expected), GET_RPN, rowNumber);
    }

    private void assertEvalRPN(double actual, String expected, int rowNumber) {
        assertResult(actual, Double.parseDouble(expected), EVAL_RPN, rowNumber);
    }

    private String removeWhitespace(String s) {
        return s.replaceAll(WHITESPACE_REGEX, "");
    }

    private void assertException(Exception actualException, String expected, String method, int rowNumber) {
        assertResult(actualException.getClass().getSimpleName(), expected, method, rowNumber);
    }

    private <T> void assertResult(T actual, T expected, String method, int rowNumber) {
        assertThat(actual).withFailMessage(
                String.format(
                """
                Test "%s" in row %s
                Actual: %s
                Expected: %s
                """, method, rowNumber, actual, expected)
        ).isEqualTo(expected);
    }

    @Test
    void testGetRPN() throws IOException {
        List<List<String>> data = getData();
        for (List<String> row : data) {
            runGetRPNTest(row.get(0), row.get(1), Integer.parseInt(row.get(3)));
        }
    }

    @Test
    void testEvalRPN() throws IOException {
        List<List<String>> data = getData();
        for (List<String> row : data) {
            runEvalRPNTest(row.get(0), row.get(2), Integer.parseInt(row.get(3)));
        }
    }
}
