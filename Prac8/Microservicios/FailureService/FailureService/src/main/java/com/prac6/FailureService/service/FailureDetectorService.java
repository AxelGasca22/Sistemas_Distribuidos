package com.prac6.FailureService.service;

import com.prac6.FailureService.client.RegistryClient;
import com.prac6.FailureService.dto.NodeInfo;
import com.prac6.FailureService.dto.UpdateStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collection;

@Service
public class FailureDetectorService {

    private static final long SUSPECT_TIMEOUT = 10000;
    private static final long DEAD_TIMEOUT = 20000;

    private final RegistryClient registryClient;

    public FailureDetectorService(RegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Scheduled(fixedRate = 5000)
    public void checkNodes() {
        Collection<NodeInfo> nodes = registryClient.getNodes();
        long now = System.currentTimeMillis();

        for (NodeInfo node : nodes) {
            long diff = now - node.getLastHeartbeat();
            String newStatus = null;

            if (diff > DEAD_TIMEOUT) {
                newStatus = "DEAD";
            } else if (diff > SUSPECT_TIMEOUT) {
                newStatus = "SUSPECT";
            } else {
                newStatus = "ACTIVE";
            }

            if (node.getStatus() == null || !node.getStatus().equalsIgnoreCase(newStatus)) {
                registryClient.updateStatus(new UpdateStatusRequest(node.getNodeId(), newStatus));
                System.out.println("[FAILURE] node=" + node.getNodeId() + " status=" + newStatus);
            }
        }
    }
}