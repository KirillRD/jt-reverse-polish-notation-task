package com.jt.rpn;

import java.util.List;

public interface CharacterProcessor {
    int processCharacter(List<String> tokens, String[] characters, int index);
}
