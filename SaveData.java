package game;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper to store persistable game state.
 */
public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public GameWord word;
    public int triesLeft;
    public Set<Character> missedLetters = new HashSet<>();

    public SaveData(GameWord w, int tries, Set<Character> missed) {
        this.word = w;
        this.triesLeft = tries;
        this.missedLetters = missed;
    }
}
