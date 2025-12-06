package game;

import java.util.Random;

public class Words implements GameWord {
    private static final long serialVersionUID = 1L;

    private String[] wordList = {
        "animals", "happiness", "computer", "birthday", "students",
        "universe", "picture", "welcome", "teacher", "independence",
        "programming", "swing", "serialization", "polymorphism", "varnika"
    };

    private String selectedWord;
    private char[] letters;

    public Words() {
        selectedWord = wordList[new Random().nextInt(wordList.length)].toLowerCase();
        letters = new char[selectedWord.length()];
    }

    // For loading custom word (used when deserializing or tests)
    public Words(String word) {
        selectedWord = word.toLowerCase();
        letters = new char[selectedWord.length()];
    }

    @Override
    public boolean guess(char c) {
        boolean correct = false;
        for (int i = 0; i < selectedWord.length(); i++) {
            if (selectedWord.charAt(i) == c) {
                letters[i] = c;
                correct = true;
            }
        }
        return correct;
    }

    @Override
    public boolean isGuessedCompletely() {
        for (char ch : letters) {
            if (ch == '\u0000') return false;
        }
        return true;
    }

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder();
        for (char ch : letters) {
            sb.append(ch == '\u0000' ? '-' : ch).append(' ');
        }
        return sb.toString().trim();
    }

    @Override
    public String getWord() {
        return selectedWord;
    }
}

