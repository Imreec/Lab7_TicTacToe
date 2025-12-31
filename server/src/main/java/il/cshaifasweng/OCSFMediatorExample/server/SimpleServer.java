package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;


public class SimpleServer extends AbstractServer {
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

    private String[] board = new String[9]; // board game
    private ConnectionToClient playerX;
    private ConnectionToClient playerO;
    private boolean isGameActive = false;
    private String currentPlayer = "X"; // X always starts

    public SimpleServer(int port) {
        super(port);

    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        // changing the object type to Message
        if (msg instanceof Message) {
            Message message = (Message) msg;
            String request = message.getMessage();

            System.out.println("Received: " + request + " from " + client);

            // handling move request
            if (request.startsWith("Move:")) {
                if (!isGameActive) return; // cannot play if there are no two players

                int index = Integer.parseInt(request.split(":")[1]);

                // check 1: is it the player's turn who sent the message?
                if ((currentPlayer.equals("X") && client != playerX) ||
                        (currentPlayer.equals("O") && client != playerO)) {
                    return; // not your turn
                }

                // check 2: is the cell empty?
                if (board[index].equals("")) {
                    // perform the move
                    board[index] = currentPlayer;

                    // check for winner
                    String winner = checkWinner();
                    if (!winner.isEmpty()) {
                        sendToAllClients(new Message("Update:" + index + ":" + board[index] + ":" + "Finished"));
                        sendToAllClients(new Message("GameOver:" + winner));
                        isGameActive = false;
                    } else if (isBoardFull()) {
                        sendToAllClients(new Message("Update:" + index + ":" + board[index] + ":" + "Finished"));
                        sendToAllClients(new Message("GameOver:Draw"));
                        isGameActive = false;
                    } else {
                        // change turn
                        currentPlayer = currentPlayer.equals("X") ? "O" : "X";
                        // send update to all: who moved and where, and who's turn now
                        sendToAllClients(new Message("Update:" + index + ":" + board[index] + ":" + currentPlayer));
                    }
                }
            }
        }
    }
	public void sendToAllClients(String message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

    @Override
    protected void clientConnected(ConnectionToClient client) {
        try {
            if (playerX == null) {
                playerX = client;
                client.sendToClient(new Message("You are Player X. Waiting for opponent..."));
            } else if (playerO == null) {
                playerO = client;
                isGameActive = true;
                resetBoard();
                client.sendToClient(new Message("You are Player O. Game started!"));
                playerX.sendToClient(new Message("Opponent found. Game started! Your turn."));
            } else {
                client.sendToClient(new Message("Room is full. You are a spectator."));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String checkWinner() {
        // כל אפשרויות הניצחון (שורות, עמודות, אלכסונים)
        int[][] wins = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // שורות
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // עמודות
                {0, 4, 8}, {2, 4, 6}             // אלכסונים
        };

        for (int[] win : wins) {
            if (!board[win[0]].equals("") &&
                    board[win[0]].equals(board[win[1]]) &&
                    board[win[1]].equals(board[win[2]])) {
                return board[win[0]]; // מחזיר "X" או "O"
            }
        }
        return ""; // אין עדיין מנצח
    }

    private boolean isBoardFull() {
        for (String s : board) {
            if (s.equals("")) return false;
        }
        return true;
    }

    // --- reset board ---
    private void resetBoard() {
        for (int i = 0; i < 9; i++) {
            board[i] = ""; // board empty
        }
    }

}
