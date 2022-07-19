import java.io.*;
import java.net.*;
import java.util.*;

// Client class
class Client {

    // driver code
    public static void main(String[] args)
    {
        // establish a connection by providing host and port
        // number
        try (Socket socket = new Socket("localhost", 1234)) {

            // writing to server
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            // reading from server
            BufferedReader in
                    = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // object of scanner class
            Scanner sc = new Scanner(System.in);
            String line = null;

            while (!"exit".equalsIgnoreCase(line)) {
                // server sends instruction
                String output = in.readLine();
                System.out.println(output);
                // reading from user
                if (output.contains("Log-In successful.") || output.contains("Running")
                        || output.contains("Exiting")  || output.contains("Please enter a valid number")
                        || output.contains("New account created successfully!")
                        || output.contains("Pending requests") || output.contains("User does not exist")
                        || output.contains("All friends:") || output.contains("Starting 30") || isNumeric(output)
                        || output.contains("Run Complete!") || output.contains("Message sent successfully!")
                        || output.contains("Users you have texts with: ") || output.contains("Most recent message: ")) {
                } else {
                    line = sc.nextLine();
                    out.println(line);
                    out.flush();
                }

            }
//
            // closing the scanner object
            sc.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean isNumeric(String num) {
        try {
            int value = Integer.parseInt(num);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
