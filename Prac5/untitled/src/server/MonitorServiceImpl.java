package server;

import common.MetricReport;
import common.Registration;
import remote.MonitorService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorServiceImpl extends UnicastRemoteObject implements MonitorService {

    private final Map<String, Registration> registeredNodes = new ConcurrentHashMap<>();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final Map<String, Integer> lastMetric = new ConcurrentHashMap<>();

    public MonitorServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void registerNode(Registration registration) throws RemoteException {
        registeredNodes.put(registration.getNodeId(), registration);
        lastHeartbeat.put(registration.getNodeId(), System.currentTimeMillis());

        System.out.println("[RMI] Nodo registrado: " + registration);
    }

    @Override
    public void sendHeartbeat(String nodeId) throws RemoteException {
        if (!registeredNodes.containsKey(nodeId)) {
            System.out.println("[RMI] Heartbeat ignorado. Nodo no registrado: " + nodeId);
            return;
        }

        lastHeartbeat.put(nodeId, System.currentTimeMillis());
        System.out.println("[RMI] Heartbeat recibido de: " + nodeId);
    }

    @Override
    public void reportMetric(MetricReport metricReport) throws RemoteException {
        if (!registeredNodes.containsKey(metricReport.getNodeId())) {
            System.out.println("[RMI] Métrica ignorada. Nodo no registrado: " + metricReport.getNodeId());
            return;
        }

        lastMetric.put(metricReport.getNodeId(), metricReport.getValue());

        String state = classifyMetric(metricReport.getValue());
        System.out.println("[RMI] Métrica recibida: " + metricReport + " -> estado=" + state);

        if ("CRITICAL".equals(state)) {
            System.out.println("[SCALE-UP] Recurso en estado crítico: " + metricReport.getType());
        } else if ("LOW".equals(state)) {
            System.out.println("[SCALE-DOWN] Recurso en estado bajo: " + metricReport.getType());
        }
    }

    private String classifyMetric(int value) {
        if (value >= 80) {
            return "CRITICAL";
        } else if (value <= 20) {
            return "LOW";
        }
        return "NORMAL";
    }
}