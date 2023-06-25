package com.jt.rpn;

public enum Symbol {
    ADDITION("+"),
    SUBTRACTION("-"),
    DIVISION("/"),
    MULTIPLICATION("*"),
    LEFT_PARENTHESIS("("),
    RIGHT_PARENTHESIS(")"),
    POINT(".");

    private final String character;

    Symbol(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }
}
