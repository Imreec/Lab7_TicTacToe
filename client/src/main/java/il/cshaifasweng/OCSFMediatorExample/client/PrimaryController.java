package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class PrimaryController {

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane gridPane;

    @FXML
    private Label playerRoleLabel;

    // helper array to save references to buttons
    private Button[] buttons = new Button[9];

    @FXML
    public void initialize() {
        // register with EventBus to receive messages
        EventBus.getDefault().register(this);

        // map buttons from GridPane to our array
        // safer approach is to scan the GridPane directly
        int index = 0;
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                // the ID of the button in FXML is "btn0", "btn1" etc.
                // extract the number from the ID
                String id = btn.getId();
                if (id != null && id.startsWith("btn")) {
                    int btnIndex = Integer.parseInt(id.substring(3));
                    buttons[btnIndex] = btn;
                }
            }
        }
    }

    // function called when any button is clicked
    @FXML
    void handleBtnClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();
        int index = Integer.parseInt(id.substring(3)); // get the number 4

        try {
            // send to server: "user wants to move to square 4"
            SimpleClient.getClient().sendToServer(new Message("Move:" + index));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // function to handle events from EventBus (messages from server)
    @Subscribe
    public void onTaskMessageEvent(TaskMessageEvent event) {
        Message msg = event.getMessage();
        String content = msg.getMessage();

        // update GUI on the main thread
        Platform.runLater(() -> {
            // type 1: update board (someone made a move)
            if (content.startsWith("Update:")) {
                String[] parts = content.split(":");
                int index = Integer.parseInt(parts[1]);  // which square
                String symbol = parts[2];                // X or O
                String nextPlayer = parts[3];            // whose turn now

                // update button
                buttons[index].setText(symbol);
                buttons[index].setDisable(true); // cannot click again

                statusLabel.setText("Current Turn: " + nextPlayer);
            }
            // type 2: game over
            else if (content.startsWith("GameOver:")) {
                String winner = content.split(":")[1];
                if (winner.equals("Draw")) {
                    statusLabel.setText("Game Over! It's a Draw!");
                } else {
                    statusLabel.setText("Game Over! The winner is: " + winner);
                }
                disableAllButtons();
            }
            // type 3: general messages (connection, waiting)
            else {
                statusLabel.setText(content);
                if (content.contains("You are Player X")) {
                    playerRoleLabel.setText("You are: Player X");
                } else if (content.contains("You are Player O")) {
                    playerRoleLabel.setText("You are: Player O");
                }
            }
        });
    }

    private void disableAllButtons() {
        for (Button btn : buttons) {
            if (btn != null) btn.setDisable(true);
        }
    }
}