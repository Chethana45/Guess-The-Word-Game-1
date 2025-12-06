package game;

import java.io.Serializable;

public interface GameWord extends Serializable {
    boolean guess(char c);
    boolean isGuessedCompletely();
    String display();
    String getWord();
}
