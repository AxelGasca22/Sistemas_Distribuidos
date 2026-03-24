package client;

import common.MetricReport;
import common.Registration;
import common.ResourceType;
import remote.MonitorService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.UUID;

public class RMIClient {

    public static void main(String[] args) {
        try {
            String host = "localhost";
            int port = 1099;

            Registry registry = LocateRegistry.getRegistry(host, port);
            MonitorService service = (MonitorService) registry.lookup("MonitorService");

            ResourceType type = ResourceType.CONTAINER;
            String nodeId = "NODE-" + UUID.randomUUID().toString().substring(0, 8);

            Registration registration = new Registration(nodeId, type, "METRIC_HEARTBEAT");
            service.registerNode(registration);

            Random random = new Random();

            while (true) {
                service.sendHeartbeat(nodeId);

                int metricValue = random.nextInt(101); // 0 a 100
                MetricReport metricReport = new MetricReport(nodeId, type, metricValue);
                service.reportMetric(metricReport);

                Thread.sleep(3000);
            }

        } catch (Exception e) {
            System.err.println("Error en el cliente RMI:");
            e.printStackTrace();
        }
    }
}