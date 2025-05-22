import java.net.*;

public class CoExistenceServer {
    public static void main(String[] args) {
        int port = 35754;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port + "...");

            while (true) {
                System.out.println("Waiting for Player 1...");
                Socket p1 = serverSocket.accept();
                System.out.println("Player 1 connected.");

                System.out.println("Waiting for Player 2...");
                Socket p2 = serverSocket.accept();
                System.out.println("Player 2 connected. Starting game session...");

                new GameSessionThread(p1, p2).start();
            }

        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
