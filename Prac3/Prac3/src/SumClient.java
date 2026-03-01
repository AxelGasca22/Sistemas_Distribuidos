import java.io.*;
import java.net.*;

public class SumClient {

    public static void main(String[] args) {

        String host = "localhost";
        int port = 5000;
        long n = 1000000;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {
            System.out.println("Conectado al servidor...");
            out.println(n);

            String response = in.readLine();
            System.out.println("Resultado recibido: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}