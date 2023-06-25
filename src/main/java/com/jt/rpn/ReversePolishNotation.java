package com.jt.rpn;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReversePolishNotation {

    private static final String WHITESPACE_REGEX = "\\s";
    private static final String PARENTHESIS_WITH_SUBTRACTION_WITHOUT_ZERO = "\\(-";
    private static final String PARENTHESIS_WITH_SUBTRACTION_WITH_ZERO = "(0-";
    private static final Set<String> DIGITS = IntStream.range(0, 10).mapToObj(String::valueOf).collect(Collectors.toSet());
    private static final Set<String> OPERATIONS = Set.of(
            Symbol.ADDITION.getCharacter(),
            Symbol.SUBTRACTION.getCharacter(),
            Symbol.DIVISION.getCharacter(),
            Symbol.MULTIPLICATION.getCharacter()
    );
    private static final Set<String> PARENTHESIS = Set.of(
            Symbol.LEFT_PARENTHESIS.getCharacter(),
            Symbol.RIGHT_PARENTHESIS.getCharacter()
    );
    private static final Map<String, CharacterProcessor> characterProcessors = new HashMap<>();
    private static final Map<String, TokenProcessor> tokenProcessors = new HashMap<>();
    private static final TokenProcessor numberTokenProcessor = (List<String> rpn, Deque<String> stack, String token) -> rpn.add(token);
    private static final Map<String, Integer> operationWeights = Map.ofEntries(
            Map.entry(Symbol.ADDITION.getCharacter(), 0),
            Map.entry(Symbol.SUBTRACTION.getCharacter(), 0),
            Map.entry(Symbol.MULTIPLICATION.getCharacter(), 1),
            Map.entry(Symbol.DIVISION.getCharacter(), 1)
    );
    private static final Map<String, OperationProcessor> operationProcessors = Map.ofEntries(
            Map.entry(Symbol.ADDITION.getCharacter(), (BigDecimal x, BigDecimal y) -> String.valueOf(x.add(y))),
            Map.entry(Symbol.SUBTRACTION.getCharacter(), (BigDecimal x, BigDecimal y) -> String.valueOf(y.subtract(x))),
            Map.entry(Symbol.MULTIPLICATION.getCharacter(), (BigDecimal x, BigDecimal y) -> String.valueOf(x.multiply(y))),
            Map.entry(Symbol.DIVISION.getCharacter(), (BigDecimal x, BigDecimal y) -> String.valueOf(y.divide(x)))
    );

    static {
        CharacterProcessor digitAndPointProcessor = (List<String> tokens, String[] characters, int index) -> {
            StringBuilder token = new StringBuilder();
            for (; characters.length > index; index++) {
                if (DIGITS.contains(characters[index])) {
                    token.append(characters[index]);
                } else if (Symbol.POINT.getCharacter().equals(characters[index])) {
                    if (token.toString().contains(Symbol.POINT.getCharacter())) throw new IllegalArgumentException("Invalid format of mathematical expression. Incorrect number format");
                    token.append(characters[index]);
                } else {
                    break;
                }
            }
            if (
                Symbol.POINT.getCharacter().equals(token.toString()) ||
                (!tokens.isEmpty() && Symbol.RIGHT_PARENTHESIS.getCharacter().equals(getLastTokenValue(tokens)))
            ) throw new IllegalArgumentException("Invalid format of mathematical expression. Incorrect number format");
            tokens.add(token.toString());
            return --index;
        };

        CharacterProcessor operationProcessor = (List<String> tokens, String[] characters, int index) -> {
            if (
                    tokens.isEmpty() ||
                    OPERATIONS.contains(getLastTokenValue(tokens)) ||
                    Symbol.LEFT_PARENTHESIS.getCharacter().equals(getLastTokenValue(tokens))
            ) throw new IllegalArgumentException("Invalid format of mathematical expression. Incorrect sequence of operations");
            tokens.add(characters[index]);
            return index;
        };

        CharacterProcessor leftParenthesisProcessor = (List<String> tokens, String[] characters, int index) -> {
            if (
                !tokens.isEmpty() &&
                !OPERATIONS.contains(getLastTokenValue(tokens)) &&
                !Symbol.LEFT_PARENTHESIS.getCharacter().equals(getLastTokenValue(tokens))
            ) throw new IllegalArgumentException("Invalid format of mathematical expression. Incorrect sequence of parenthesis");
            tokens.add(characters[index]);
            return index;
        };

        CharacterProcessor rightParenthesisProcessor = (List<String> tokens, String[] characters, int index) -> {
            if (
                !tokens.isEmpty() &&
                (
                    OPERATIONS.contains(getLastTokenValue(tokens)) ||
                    Symbol.LEFT_PARENTHESIS.getCharacter().equals(getLastTokenValue(tokens))
                )
            ) throw new IllegalArgumentException("Invalid format of mathematical expression. Incorrect sequence of parenthesis");
            tokens.add(characters[index]);
            return index;
        };

        addProcessors(characterProcessors, Stream.concat(DIGITS.stream(), Stream.of(Symbol.POINT.getCharacter())).collect(Collectors.toSet()), digitAndPointProcessor);
        addProcessors(characterProcessors, OPERATIONS, operationProcessor);
        addProcessors(characterProcessors, Collections.singleton(Symbol.LEFT_PARENTHESIS.getCharacter()), leftParenthesisProcessor);
        addProcessors(characterProcessors, Collections.singleton(Symbol.RIGHT_PARENTHESIS.getCharacter()), rightParenthesisProcessor);

        TokenProcessor operationTokenProcessor = (List<String> rpn, Deque<String> stack, String token) -> {
            while (!stack.isEmpty() && OPERATIONS.contains(stack.peek()) && operationWeights.get(token) <= operationWeights.get(stack.peek())) {
                rpn.add(stack.pop());
            }
            stack.push(token);
        };

        TokenProcessor leftParenthesisTokenProcessor = (List<String> rpn, Deque<String> stack, String token) -> stack.push(token);

        TokenProcessor rightParenthesisTokenProcessor = (List<String> rpn, Deque<String> stack, String token) -> {
            while (!stack.isEmpty() && !stack.peek().equals(Symbol.LEFT_PARENTHESIS.getCharacter())) {
                rpn.add(stack.pop());
            }
            if (stack.isEmpty() || !Symbol.LEFT_PARENTHESIS.getCharacter().equals(stack.peek())) throw new IllegalArgumentException("Invalid format of mathematical expression. There is no closing parenthesis");
            stack.pop();
        };

        addProcessors(tokenProcessors, OPERATIONS, operationTokenProcessor);
        addProcessors(tokenProcessors, Collections.singleton(Symbol.LEFT_PARENTHESIS.getCharacter()), leftParenthesisTokenProcessor);
        addProcessors(tokenProcessors, Collections.singleton(Symbol.RIGHT_PARENTHESIS.getCharacter()), rightParenthesisTokenProcessor);
    }

    private static <F> void addProcessors(Map<String, F> processors, Iterable<String> characters, F processor) {
        for (String character : characters) {
            processors.put(character, processor);
        }
    }

    private static String getLastTokenValue(List<String> tokens) {
        return tokens.get(tokens.size() - 1);
    }

    public List<String> get(String expression) {
        expression = isNullOrBlank(expression);
        expression = replaceSubtraction(expression);
        List<String> tokens = getTokens(expression);
        return tokensToRPN(tokens);
    }

    public double eval(String expression) {
        List<String> tokens = get(expression);
        Deque<String> stack = new ArrayDeque<>();
        for (String token : tokens) {
            if (!OPERATIONS.contains(token)) {
                stack.push(token);
                continue;
            }
            BigDecimal x = getLastStackValue(stack);
            BigDecimal y = getLastStackValue(stack);
            stack.push(getOperationProcessor(token).processOperation(x, y));
        }
        return Double.parseDouble(stack.pop());
    }

    private String isNullOrBlank(String s) {
        if (s != null) {
            s = s.replaceAll(WHITESPACE_REGEX, "");
            if (!s.isBlank()) return s;
        }
        throw new IllegalArgumentException("Mathematical expression is empty");
    }

    private String replaceSubtraction(String s) {
        s = s.replaceAll(PARENTHESIS_WITH_SUBTRACTION_WITHOUT_ZERO, PARENTHESIS_WITH_SUBTRACTION_WITH_ZERO);
        if (s.startsWith(Symbol.SUBTRACTION.getCharacter())) s = 0 + s;
        return s;
    }

    private List<String> getTokens(String expression) {
        List<String> tokens = new ArrayList<>();
        String[] characters = expression.split("");
        CharacterProcessor characterProcessor;
        for (int i = 0; characters.length > i; i++) {
            characterProcessor = getCharacterProcessor(characters[i]);
            i = characterProcessor.processCharacter(tokens, characters, i);
        }
        return tokens;
    }

    private CharacterProcessor getCharacterProcessor(String character) {
        return Optional.ofNullable(characterProcessors.get(character)).orElseThrow(() -> new IllegalArgumentException("Invalid format of mathematical expression. There are invalid characters"));
    }

    private List<String> tokensToRPN(List<String> tokens) {
        List<String> rpn = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        TokenProcessor tokenProcessor;
        for (String token : tokens) {
            tokenProcessor = getTokenProcessor(token);
            tokenProcessor.processToken(rpn, stack, token);
        }
        while (!stack.isEmpty()) {
            if (PARENTHESIS.contains(stack.peek())) throw new IllegalArgumentException("Invalid format of mathematical expression. Incorrect use of parentheses");
            rpn.add(stack.pop());
        }
        return rpn;
    }

    private TokenProcessor getTokenProcessor(String token) {
        return Optional.ofNullable(tokenProcessors.get(token)).orElse(numberTokenProcessor);
    }

    private BigDecimal getLastStackValue(Deque<String> stack) {
        return new BigDecimal(stack.pop());
    }

    private OperationProcessor getOperationProcessor(String token) {
        return Optional.ofNullable(operationProcessors.get(token)).orElseThrow(() -> new IllegalArgumentException("Invalid format of mathematical expression. There are invalid characters"));
    }
}
