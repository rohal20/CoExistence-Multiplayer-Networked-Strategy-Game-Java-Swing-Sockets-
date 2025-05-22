import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoExistenceClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 35754;

        try (
                Socket socket = new Socket(hostname, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server.");

            // Create a flag to control the reader thread
            AtomicBoolean running = new AtomicBoolean(true);

            // Start a separate thread to continuously read from the server
            Thread readerThread = new Thread(() -> {
                try {
                    while (running.get()) {
                        String frame = readFrame(in);
                        if (frame != null) {
                            clearConsole();
                            System.out.print(frame);
                            // Check if the frame contains a game over message
                            if (frame.contains("GAME OVER")) {
                                running.set(false); // Stop the reader thread
                                System.exit(0); // Exit the program
                            }
                            System.out.print("Enter move (e.g. AB to attack from A to B, or PS to pass): ");
                        }
                        Thread.sleep(50); // Small delay to prevent CPU overuse
                    }
                } catch (IOException | InterruptedException e) {
                    if (running.get()) {
                        System.out.println("Error reading from server: " + e.getMessage());
                    }
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Main loop for handling user input
            while (true) {
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    out.println(input);
                    out.flush();
                }
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String readFrame(BufferedReader in) throws IOException {
        StringBuilder frame = new StringBuilder();
        String line;

        // Read until we find the frame start
        while (in.ready() && (line = in.readLine()) != null) {
            if (line.startsWith("/---")) {
                frame.append(line).append("\n");
                break;
            }
        }

        // If we found a frame start, read until frame end
        if (frame.length() > 0) {
            while ((line = in.readLine()) != null) {
                frame.append(line).append("\n");
                if (line.startsWith("\\---")) {
                    break;
                }
            }
        }

        return frame.length() > 0 ? frame.toString() : null;
    }
}
