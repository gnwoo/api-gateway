package com.gnwoo.apigateway.data.repo;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WsCommunicationRepo {
    private Map<Long, Map<String, SocketIOClient>> wsCommunicationRepo = new ConcurrentHashMap<>();

    public void saveClientTerminal(Long uuid, String client_session_id, SocketIOClient socket_io_client) {
        Map<String, SocketIOClient> client_terminals =  wsCommunicationRepo.getOrDefault(uuid, new HashMap<>());
        client_terminals.put(client_session_id, socket_io_client);
        wsCommunicationRepo.put(uuid, client_terminals);
    }

    public Map<String, SocketIOClient> getAllClientTerminals(Long uuid) {
        return wsCommunicationRepo.get(uuid);
    }

    public void deleteClientTerminal(Long uuid, String client_session_id) {
        wsCommunicationRepo.get(uuid).remove(client_session_id);
    }
}
