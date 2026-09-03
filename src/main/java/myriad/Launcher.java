package myriad;

import javafx.application.Application;

/**
 * Starts the Myriad GUI.
 *
 * The launch call lives here, in a class that does not itself extend
 * Application, because a JVM told to run an Application subclass directly
 * checks for JavaFX on the module path and quits with "JavaFX runtime
 * components are missing" when — as here — JavaFX is on the classpath
 * instead. Launching from an unrelated class skips that check.
 */
public class Launcher {

    /**
     * Starts the JavaFX application.
     *
     * @param args passed through to JavaFX; Myriad itself ignores them.
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
