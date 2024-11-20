import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Rodrigo Esquide Gómez
 */
class EchoClient {
    // Connection to the server
    private Socket clientSocket;
    // BufferedReader to read data
    private BufferedReader input;
    // PrintWriter to send data
    private PrintWriter output;
    // 10x10 board simulating the battlefield
    private String[][] board = new String[10][10];
    // Scanner object creation
    static Scanner keyboard = new Scanner(System.in);

    // Client constructor
    public EchoClient(String ip, int port) {
        try {
            // Establish connection with the server
            clientSocket = new Socket(ip, port);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
            // Calls the initializeBoard method
            initializeBoard();
            // Calls the placeShipsRandomly method
            placeShipsRandomly();
        } catch (IOException e) {
            System.err.println("Cannot connect to the server.");
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
        System.out.println("Client's Board:");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Method that requests coordinates from the user to attack the server
     */
    public void sendAttack() {
        System.out.print("Enter X coordinate to attack (1-10): ");
        int x = Integer.parseInt(keyboard.nextLine()) - 1;
        System.out.print("Enter Y coordinate to attack (1-10): ");
        int y = Integer.parseInt(keyboard.nextLine()) - 1;
        // Sends the coordinates to the server via PrintWriter
        output.println(x + "," + y);
    }

    /**
     * Method to check if the received attack hit a ship
     * 
     * @param coordinates
     * @return String - indicates whether it hit a ship or not
     */
    public String receiveAttack(String coordinates) {
        // Splits the coordinates into x and y using split
        String[] parts = coordinates.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        // Marks the ship as hit if it was hit
        if (board[x][y].equals("⛵️")) {
            board[x][y] = "💥";
            return "Hit! Game over.";
        } else {
            // Marks the cell as missed
            board[x][y] = "💦";
            return "Missed! Water";
        }
    }

    /**
     * Method to read a message from the server
     * 
     * @return
     */
    public String receiveMessage() {
        try {
            // Reads the message from the server with BufferedReader
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Method to send a message to the server via PrintWriter
     * 
     * @param message
     */
    public void sendMessage(String line) {
        output.println(line);
    }

    /**
     * Method to close the connections: BufferedReader, PrintWriter, and client socket
     */
    public void closeClientTCP() {
        try {
            output.close();
            input.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected.");
    }
}

/**
 * Main class containing the main method
 */
public class TCPClient {
    public static void main(String[] args) {
        // Creates an instance of EchoClient and connects to the server.
        EchoClient client = new EchoClient("localhost", 5555);
        String response;
        boolean play = true;
        while (play) {
            System.out.println("Your turn:");
            client.displayBoard();

            // The client attacks the server, requests coordinates, and sends them
            client.sendAttack();
            // Receives the server's response
            response = client.receiveMessage();
            System.out.println("Server: " + response);
            System.out.println();
            // If a ship is hit, the client wins, and the game ends
            if (response.contains("Hit!")) {
                System.out.println("You won!");
                client.sendMessage("Game over");
                play = false;
            }

            // If no ship is sunk, the client waits and receives the server's attack
            if (play) {
                System.out.println("Waiting for the server's attack...");
                // Receives the server's attack
                response = client.receiveMessage();
                // Checks if the attack was successful or not
                String result = client.receiveAttack(response);
                // Sends the result to the server
                client.sendMessage(result);
                // If the server sinks a ship, the client loses, and the game ends
                if (result.contains("Game over")) {
                    System.out.println("You lost, a ship was sunk.");
                    client.displayBoard();
                    client.sendMessage("Game over");
                    play = false;
                }
            }
        }
        // Calls the method to close the connection
        client.closeClientTCP();
    }
}
