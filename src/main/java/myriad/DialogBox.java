package myriad;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * Represents one message in the conversation: the speaker's picture beside
 * the text they said. The user's messages sit on the right and Myriad's on
 * the left, so that a glance down the window shows who said what.
 *
 * The layout comes from view/DialogBox.fxml, which is an fx:root file: each
 * instance makes itself both the root and the controller of that file, so
 * that many dialog boxes can be built from one layout description.
 */
public class DialogBox extends HBox {

    /** Width and height the picture is drawn at, matching DialogBox.fxml. */
    private static final double PICTURE_SIZE = 80.0;

    /**
     * Room the text cannot use: the padding and spacing set in DialogBox.fxml,
     * plus the bubble's own padding and border from dialog-box.css. The CSS
     * insets are counted here because the binding below works from the box's
     * own width, which does not know about them.
     */
    private static final double BOX_CHROME_WIDTH = 56.0;

    /** Room left for the picture, the spacing and the padding around them. */
    private static final double NON_TEXT_WIDTH = PICTURE_SIZE + BOX_CHROME_WIDTH;

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing message next to picture, laid out with the
     * picture on the right.
     *
     * @param message what the speaker said.
     * @param picture the speaker's display picture, or null if none is available.
     */
    private DialogBox(String message, Image picture) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            // The layout file ships inside the application, so a failure here
            // means a broken build rather than anything the user can act on.
            e.printStackTrace();
        }

        dialog.setText(message);
        displayPicture.setImage(picture);
        roundDisplayPicture(picture);

        // A Label reports the width of its longest line as the width it needs,
        // and that demand travels up to the window, which grows to meet it.
        // Capping the width at whatever room this box has forces the text to
        // wrap instead, so a long reply never widens the window.
        dialog.maxWidthProperty().bind(widthProperty().subtract(NON_TEXT_WIDTH));
    }

    /**
     * Crops the picture to a centred square and clips it to a circle, so that
     * pictures of any shape are shown as discs of the same size. Cropping the
     * source first matters: the view keeps the picture's proportions, so a wide
     * picture would otherwise be drawn shorter than the circle and be cut off
     * along the top and bottom.
     *
     * @param picture the image behind the view, or null when there is none.
     */
    private void roundDisplayPicture(Image picture) {
        if (picture == null) {
            return;
        }

        double side = Math.min(picture.getWidth(), picture.getHeight());
        displayPicture.setViewport(new Rectangle2D(
                (picture.getWidth() - side) / 2,
                (picture.getHeight() - side) / 2,
                side,
                side));

        double radius = PICTURE_SIZE / 2;
        displayPicture.setClip(new Circle(radius, radius, radius));
    }

    /**
     * Mirrors this dialog box, so that the picture is on the left of the text
     * rather than the right, and restyles the bubble to match. This is the one
     * place the two speakers differ, so it is where the style class that tells
     * them apart is added.
     */
    private void flip() {
        ObservableList<Node> nodes = FXCollections.observableArrayList(getChildren());
        Collections.reverse(nodes);
        getChildren().setAll(nodes);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
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
