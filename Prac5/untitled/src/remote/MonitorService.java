package remote;

import common.MetricReport;
import common.Registration;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MonitorService extends Remote {

    void registerNode(Registration registration) throws RemoteException;

    void sendHeartbeat(String nodeId) throws RemoteException;

    void reportMetric(MetricReport metricReport) throws RemoteException;
}