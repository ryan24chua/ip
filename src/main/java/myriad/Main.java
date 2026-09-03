package myriad;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Displays the Myriad window. The layout itself lives in
 * view/MainWindow.fxml and the behaviour in MainWindow, so this class is
 * left with the wiring: load the layout, hand the controller a chatbot
 * session, and show the window.
 */
public class Main extends Application {

    /**
     * The session behind the window, pointed at the same data file the console
     * front end uses, so that both talk to one saved task list.
     */
    private final Myriad myriad = new Myriad(new File("data", "myriad.txt").getPath(), false);

    /**
     * Builds and shows the window when JavaFX has finished starting up.
     *
     * @param stage the window JavaFX supplies for this application.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane root = fxmlLoader.load();
            MainWindow controller = fxmlLoader.getController();

            stage.setScene(new Scene(root));
            stage.setTitle("Myriad");
            stage.setMinHeight(220.0);
            stage.setMinWidth(417.0);

            controller.setMyriad(myriad);
            stage.show();
            controller.focusInput();
        } catch (IOException e) {
            // The layout file ships inside the application, so a failure here
            // means a broken build rather than anything the user can act on.
            e.printStackTrace();
        }
    }
}
