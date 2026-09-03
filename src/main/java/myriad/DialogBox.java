package myriad;

import java.io.InputStream;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Represents one message in the conversation: the speaker's picture beside
 * the text they said. The user's messages sit on the right and Myriad's on
 * the left, so that a glance down the window shows who said what.
 */
public class DialogBox extends HBox {

    private static final double PICTURE_SIZE = 80.0;

    /** Room left for the picture, the spacing and the padding around them. */
    private static final double NON_TEXT_WIDTH = PICTURE_SIZE + 32.0;

    private final Label text;
    private final ImageView displayPicture;

    /**
     * Creates a dialog box showing message next to picture, laid out with the
     * picture on the right.
     *
     * @param message what the speaker said.
     * @param picture the speaker's display picture, or null if none is available.
     */
    private DialogBox(String message, Image picture) {
        text = new Label(message);
        displayPicture = new ImageView(picture);

        text.setWrapText(true);
        // A Label reports the width of its longest line as the width it needs,
        // and that demand travels up to the window, which grows to meet it.
        // Capping the width at whatever room this box has forces the text to
        // wrap instead, so a long reply never widens the window.
        text.maxWidthProperty().bind(widthProperty().subtract(NON_TEXT_WIDTH));
        text.setMinWidth(0.0);

        displayPicture.setFitWidth(PICTURE_SIZE);
        displayPicture.setFitHeight(PICTURE_SIZE);
        displayPicture.setPreserveRatio(true);

        setAlignment(Pos.TOP_RIGHT);
        setSpacing(8.0);
        setPadding(new Insets(8.0));
        getChildren().addAll(text, displayPicture);
    }

    /**
     * Mirrors this dialog box, so that the picture is on the left of the text
     * rather than the right.
     */
    private void flip() {
        ObservableList<Node> nodes = FXCollections.observableArrayList(getChildren());
        Collections.reverse(nodes);
        getChildren().setAll(nodes);
        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * Returns the picture stored at the given location on the classpath, or
     * null when there is no file there. A missing picture leaves a blank space
     * instead of stopping the window from opening.
     *
     * @param resourcePath classpath location of the image, e.g. "/images/DaUser.png".
     * @return the loaded image, or null if the file is absent.
     */
    public static Image loadPicture(String resourcePath) {
        InputStream stream = DialogBox.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }

    /**
     * Returns a dialog box for something the user said.
     *
     * @param message what the user typed.
     * @param picture the user's display picture, or null if none is available.
     * @return a dialog box with the picture on the right.
     */
    public static DialogBox getUserDialog(String message, Image picture) {
        return new DialogBox(message, picture);
    }

    /**
     * Returns a dialog box for something Myriad said.
     *
     * @param message the chatbot's reply.
     * @param picture Myriad's display picture, or null if none is available.
     * @return a dialog box with the picture on the left.
     */
    public static DialogBox getMyriadDialog(String message, Image picture) {
        DialogBox dialogBox = new DialogBox(message, picture);
        dialogBox.flip();
        return dialogBox;
    }
}
