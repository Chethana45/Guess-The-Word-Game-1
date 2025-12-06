package game;

/**
 * Simple timer thread that decrements every second. Uses a listener callback to push updates to GUI.
 */
public class TimerThread extends Thread {
    private volatile int secondsLeft;
    private volatile boolean running = true;

    public interface Listener {
        void onTick(int secondsLeft);
        void onFinish();
    }

    private final Listener listener;

    public TimerThread(int seconds, Listener listener) {
        this.secondsLeft = seconds;
        this.listener = listener;
        setDaemon(true);
    }

    public void stopTimer() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running && secondsLeft > 0) {
                Thread.sleep(1000);
                secondsLeft--;
                if (listener != null) listener.onTick(secondsLeft);
            }
            if (running && secondsLeft <= 0 && listener != null) listener.onFinish();
        } catch (InterruptedException ex) {
            // Thread interrupted — stop silently
            Thread.currentThread().interrupt();
        }
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }
}
