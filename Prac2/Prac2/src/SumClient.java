import java.io.*;
import java.net.*;

public class SumClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        long n = 1_000_000; // puedes cambiarlo

        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("Conectado al servidor " + host + ":" + port);
            System.out.println("Enviando solicitud: sumar 1.." + n);

            out.println(n); // mandamos N

            String response = in.readLine(); // recibimos suma
            System.out.println("Respuesta del servidor (suma total): " + response);

        } catch (IOException e) {
            System.out.println("Error en cliente: " + e.getMessage());
        }
    }
}
