import java.io.*;
import java.net.*;

class GameSessionThread extends Thread {
    private Socket player1, player2;
    private GameState gameState;

    public GameSessionThread(Socket p1, Socket p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.gameState = new GameState(true);
    }

    public void run() {
        try (
                PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
                PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
                BufferedReader in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()))) {

            // Send initial frames to both players
            sendFramesToBothPlayers(out1, out2);

            while (!gameState.isGameOver()) {
                int currentPlayer = gameState.getTurn();
                BufferedReader currentIn = (currentPlayer == 0) ? in1 : in2;
                PrintWriter currentOut = (currentPlayer == 0) ? out1 : out2;
                PrintWriter otherOut = (currentPlayer == 0) ? out2 : out1;

                String input = currentIn.readLine();
                if (input == null) {
                    break;
                }

                input = input.trim().toUpperCase();
                boolean validMove = gameState.processCommand(input);

                // Always send updated frames to both players
                sendFramesToBothPlayers(out1, out2);

                // Add a small delay to ensure frames are received in order
                Thread.sleep(50);
            }

            // Send final game state to both players
            sendFramesToBothPlayers(out1, out2);

        } catch (IOException | InterruptedException e) {
            System.out.println("Game session ended: " + e.getMessage());
        } finally {
            try {
                player1.close();
                player2.close();
            } catch (IOException e) {
                System.out.println("Error closing sockets: " + e.getMessage());
            }
        }
    }

    private void sendFrameToPlayer(PrintWriter out, String frame) {
        if (out != null) {
            out.println(frame);
            out.flush();
        }
    }

    private void sendFramesToBothPlayers(PrintWriter out1, PrintWriter out2) {
        // Generate frames for both players
        String frame1 = gameState.generateFrame(true); // Player 1's view
        String frame2 = gameState.generateFrame(false); // Player 2's view

        // Send frames to respective players
        sendFrameToPlayer(out1, frame1);
        sendFrameToPlayer(out2, frame2);

        // Ensure the output is flushed
        if (out1 != null)
            out1.flush();
        if (out2 != null)
            out2.flush();
    }
}
