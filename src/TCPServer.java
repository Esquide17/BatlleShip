import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Rodrigo Esquide Gómez
 */
class EchoServer {
    // Connection with the client
    private Socket clientSocket;
    // Socket to accept incoming connections
    private ServerSocket serverSocket;
    // BufferedReader to read data
    private BufferedReader input;
    // PrintWriter to send data
    private PrintWriter output;
    // 10x10 board simulating the battlefield
    private String[][] board = new String[10][10];
    // Scanner object creation
    static Scanner keyboard = new Scanner(System.in);

    // Server constructor
    public EchoServer(int port) {
        try {
            // The server listens on the port passed as a parameter
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for the client...");
            // Accepts the client's connection
            clientSocket = serverSocket.accept();
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
            // Calls the initializeBoard method
            initializeBoard();
            // Calls the placeShipsRandomly method
            placeShipsRandomly();
        } catch (IOException e) {
            System.err.println("Cannot listen on the port.");
            System.exit(-1);
        }
    }

    /**
     * Method that initializes the board with water
     */
    private void initializeBoard() {
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                board[i][j] = "🌊";
    }

    /**
     * Method that randomly places 10 ships on the board
     */
    private void placeShipsRandomly() {
        Random random = new Random();
        int ships = 10;
        while (ships > 0) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);
            if (board[x][y].equals("🌊")) {
                board[x][y] = "⛵️";
                ships--;
            }
        }
    }

    /*
     * Method to print the board
     */
    public void displayBoard() {
        System.out.println("Server's Board:");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Method to receive the client's attack. Receives the coordinates to be
     * attacked and calls processAttack to check if it hit a ship or not.
     */
    public void receiveClientAttack() {
        String coordinates = receiveMessage();
        if (coordinates.equals("Game over")) {
            System.out.println("The client has won. Game over.");
            sendMessage("Game over");
            // Ends the method execution here, skipping the remaining code.
            return;
        }
        String result = processAttack(coordinates);
        sendMessage(result);
        displayBoard();
    }

    /**
     * Method to check if the client's attack hit a ship
     * 
     * @param coordinates
     * @return String - indicates whether it hit a ship or not
     */
    private String processAttack(String coordinates) {
        String[] parts = coordinates.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);

        if (board[x][y].equals("⛵️")) {
            board[x][y] = "💥";
            return "Hit! Game over.";
        } else {
            board[x][y] = "💦";
            return "Missed! Water";
        }
    }

    /**
     * Method to read a message from the client
     * 
     * @return
     */
    public String receiveMessage() {
        try {
            // Reads the message from the client using BufferedReader
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Method to send a message to the client via PrintWriter
     * 
     * @param message
     */
    public void sendMessage(String line) {
        output.println(line);
    }

    /**
     * Method to close connections: BufferedReader, PrintWriter, client socket, and
     * server socket
     */
    public void closeServerTCP() {
        try {
            output.close();
            input.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server disconnected.");
    }

    /*
     * Method to check if the client has won
     */
    public boolean checkVictory() {
        // Checks if there are any sunk ships on the board
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j].equals("💥")) {
                    // If there are any, returns true
                    return true;
                }
            }
        }
        // If no ships are sunk, returns false
        return false;
    }
}

/**
 * Main class containing the main method
 */
public class TCPServer {
    // Create a static scanner object
    static Scanner keyboard = new Scanner(System.in);

    public static void main(String[] args) {
        // Creates the server and starts listening on port 5555.
        EchoServer server = new EchoServer(5555);
        boolean play = true;

        while (play) {
            // Wait for the client's attack
            System.out.println("Waiting for the client's attack...");
            server.receiveClientAttack();

            // If the client has won, terminate the game
            if (!server.checkVictory()) {
                // The server attacks by sending coordinates like the client
                System.out.println("Your turn:");
                System.out.print("Enter X coordinate to attack (1-10): ");
                int x = Integer.parseInt(keyboard.nextLine()) - 1;
                System.out.print("Enter Y coordinate to attack (1-10): ");
                int y = Integer.parseInt(keyboard.nextLine()) - 1;
                
                // Send coordinates to the client via PrintWriter
                server.sendMessage(x + "," + y);
                
                // Receive the client's response
                String response = server.receiveMessage();
                System.out.println("Client's response: " + response);

                // Check if a ship was sunk
                if (response.contains("Game over") || response.contains("Hit!")) {
                    System.out.println("You won!");
                    server.sendMessage("Game over");
                    play = false; 
                }
            } else {
                System.out.println("The client has won!");
                server.sendMessage("Game over");
                play = false;
            }
        }
        // Call the method to close the connection
        server.closeServerTCP();
        keyboard.close(); 
    }
}
