package myriad;

import java.io.File;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Displays the Myriad window: a scrolling transcript of the conversation
 * above, and a text field with a Send button along the bottom. Each line the
 * user sends is answered by the same chatbot session the console front end
 * runs, so both talk to one saved task list.
 */
public class Main extends Application {

    private static final double WINDOW_WIDTH = 400.0;
    private static final double WINDOW_HEIGHT = 600.0;
    private static final double INPUT_HEIGHT = 40.0;
    private static final double SEND_BUTTON_WIDTH = 76.0;

    /** How long the farewell stays on screen before the window closes. */
    private static final Duration FAREWELL_PAUSE = Duration.seconds(1.5);

    private final Myriad myriad = new Myriad(new File("data", "myriad.txt").getPath(), false);
    private final Image userPicture = DialogBox.loadPicture("/images/DaUser.png");
    private final Image myriadPicture = DialogBox.loadPicture("/images/DaMyriad.png");

    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;

    /**
     * Builds and shows the window when JavaFX has finished starting up.
     *
     * @param stage the window JavaFX supplies for this application.
     */
    @Override
    public void start(Stage stage) {
        AnchorPane mainLayout = createLayout();
        Scene scene = new Scene(mainLayout);

        stage.setScene(scene);
        stage.setTitle("Myriad");
        stage.setMinHeight(220.0);
        stage.setMinWidth(417.0);
        stage.show();

        showMyriadMessage(myriad.getGreeting());
        // Requested after the window is up, so the user can type straight away.
        userInput.requestFocus();
    }

    /**
     * Assembles the controls into the window layout and returns its root.
     *
     * The pieces are anchored rather than merely positioned so that they
     * follow the edges of the window when the user resizes it: the scroll
     * pane stretches in both directions, the text field stretches sideways,
     * and the button stays pinned to the bottom-right corner.
     *
     * @return the root of the assembled layout.
     */
    private AnchorPane createLayout() {
        dialogContainer = new VBox();
        scrollPane = new ScrollPane(dialogContainer);
        userInput = new TextField();
        sendButton = new Button("Send");

        scrollPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT - INPUT_HEIGHT);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // The transcript may hold more than fits; that is what the scroll bar is
        // for, so the pane is told it may shrink instead of passing the content's
        // size demand up to the window.
        scrollPane.setMinWidth(0.0);
        scrollPane.setMinHeight(0.0);

        // The transcript is read, not edited, so it is kept out of the focus
        // order; otherwise it takes the focus on startup and typing goes nowhere.
        scrollPane.setFocusTraversable(false);

        // Keeps the newest message in view: the scroll position is tied to the
        // height of the transcript, which grows every time a box is added.
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());

        userInput.setPromptText("Type a command...");
        userInput.setPrefSize(WINDOW_WIDTH - SEND_BUTTON_WIDTH, INPUT_HEIGHT);
        sendButton.setPrefSize(SEND_BUTTON_WIDTH, INPUT_HEIGHT);

        // Both the Send button and the Enter key send the line, because either
        // is a natural thing for the user to reach for.
        sendButton.setOnAction(event -> handleUserInput());
        userInput.setOnAction(event -> handleUserInput());

        AnchorPane mainLayout = new AnchorPane(scrollPane, userInput, sendButton);
        mainLayout.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        mainLayout.setMinSize(0.0, 0.0);

        AnchorPane.setTopAnchor(scrollPane, 1.0);
        AnchorPane.setBottomAnchor(scrollPane, INPUT_HEIGHT + 1.0);
        AnchorPane.setLeftAnchor(scrollPane, 1.0);
        AnchorPane.setRightAnchor(scrollPane, 1.0);

        AnchorPane.setBottomAnchor(userInput, 1.0);
        AnchorPane.setLeftAnchor(userInput, 1.0);
        AnchorPane.setRightAnchor(userInput, SEND_BUTTON_WIDTH);

        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);

        return mainLayout;
    }

    /**
     * Sends whatever the user typed to the chatbot, and adds both their line
     * and the reply to the transcript. A blank line is ignored rather than
     * answered with an error, since pressing Enter on an empty field is a
     * slip rather than a command.
     */
    private void handleUserInput() {
        String input = userInput.getText();
        userInput.clear();
        if (input.isBlank()) {
            return;
        }

        String response = myriad.getResponse(input);
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input, userPicture));
        showMyriadMessage(response);

        if (myriad.isExitRequested()) {
            closeAfterFarewell();
        }
    }

    /**
     * Adds one of Myriad's messages to the transcript.
     *
     * @param message the text to show.
     */
    private void showMyriadMessage(String message) {
        dialogContainer.getChildren().add(DialogBox.getMyriadDialog(message, myriadPicture));
    }

    /**
     * Closes the window a moment after an exit command, so that the farewell
     * is readable rather than flashing past as the window disappears.
     */
    private void closeAfterFarewell() {
        userInput.setDisable(true);
        sendButton.setDisable(true);

        PauseTransition pause = new PauseTransition(FAREWELL_PAUSE);
        pause.setOnFinished(event -> Platform.exit());
        pause.play();
    }
}
