package com.gnwoo.apigateway;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.gnwoo.apigateway.data.repo.WsCommunicationRepo;
import com.gnwoo.apigateway.data.repo.WsSessionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChatMessageController {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageController.class);

    private final SocketIONamespace namespace;

    @Autowired
    private WsSessionRepo wsSessionRepo;

    @Autowired
    private WsCommunicationRepo wsCommunicationRepo;

    @Autowired
    private ForwardChatMessageService forwardChatMessageService;

    @Autowired
    public ChatMessageController(SocketIOServer server) {
        this.namespace = server.addNamespace("/chat");
        this.namespace.addConnectListener(onConnected());
        this.namespace.addDisconnectListener(onDisconnected());
        this.namespace.addEventListener("sendChatMessage", ChatMessage.class, onChatReceived());
    }

    private DataListener<ChatMessage> onChatReceived() {
        return (client, data, ackSender) -> {
            log.info("Client[{}] - Received chat message '{}'", client.getSessionId().toString(), data);
            // forward chat message to message service
            ForwardChatMessageResponse forwardChatMessageResponse = forwardChatMessageService.forwardChatMessage(data);

            // notify all online receiver terminals to pull new chat messages
            if(forwardChatMessageResponse != null && forwardChatMessageResponse.getReceiverUuid() != null) {
                Long receiver_uuid = forwardChatMessageResponse.getReceiverUuid();
                Map<String, SocketIOClient> receiver_terminals =
                        wsCommunicationRepo.getAllClientTerminals(receiver_uuid);
                if(receiver_terminals != null) {
                    for(SocketIOClient receiver_terminal : receiver_terminals.values()) {
                        receiver_terminal.sendEvent("new_chat_message_notification", "new chat message");
                    }
                } else {
                    // otherwise, no receiver terminals online, do nothing
                    System.out.println(receiver_uuid + "'s all terminals are offline!");
                }
            } else {
                System.out.println("no such receiver!");
            }
        };
    }

    private ConnectListener onConnected() {
        return client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            log.info("Client[{}] - Connected to chat module through '{}'", client.getSessionId().toString(),
                    handshakeData.getUrl());

            String ws_session_token = getWsSessionFromCookie(client.getHandshakeData().getHttpHeaders().get("Cookie"));
            if(ws_session_token != null) {
                Long client_uuid = wsSessionRepo.findSessionByToken(ws_session_token);
                if(client_uuid != null) {
                    System.out.println("ws session authorized");
                    wsCommunicationRepo.saveClientTerminal(client_uuid, client.getSessionId().toString(), client);
                } else {
                    System.out.println("Unauthorized ws session");
                    client.disconnect();
                }
            } else {
                System.out.println("Unauthorized ws session");
                client.disconnect();
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            log.info("Client[{}] - Disconnected from chat module.", client.getSessionId().toString());
        };
    }

    private String getWsSessionFromCookie(String cookies) {
        String[] cookie_arr = cookies.split("; ");
        for(String cookie : cookie_arr) {
            if(cookie.startsWith("WSSESSION=")) {
                return cookie.substring(10);
            }
        }
        return null;
    }
}
