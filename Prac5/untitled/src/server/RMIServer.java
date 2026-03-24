package server;

import remote.MonitorService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {

    public static void main(String[] args) {
        try {
            int port = 1099;

            MonitorService service = new MonitorServiceImpl();

            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("MonitorService", service);

            System.out.println("======================================");
            System.out.println("Servidor RMI iniciado correctamente");
            System.out.println("Puerto: " + port);
            System.out.println("Servicio publicado como: MonitorService");
            System.out.println("======================================");
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor RMI:");
            e.printStackTrace();
        }
    }
}