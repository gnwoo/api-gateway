package com.gnwoo.apigateway;

import com.google.gson.Gson;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ForwardChatMessageService {
    private final RestTemplate restTemplate;

    public ForwardChatMessageService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ForwardChatMessageResponse forwardChatMessage(ChatMessage chatMessage) {
        String url = "http://localhost:8096/push-chat-message";

        // create headers
        HttpHeaders headers = new HttpHeaders();
        // set `content-type` header
        headers.setContentType(MediaType.APPLICATION_JSON);
        // set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> map = new HashMap<>();
        map.put("senderUsername", chatMessage.getSenderUsername());
        map.put("receiverUsername", chatMessage.getReceiverUsername());
        map.put("message", chatMessage.getMessage());

        // build the request
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);

        // send POST request
        try {
            ResponseEntity<ForwardChatMessageResponse> response = this.restTemplate.postForEntity(url, entity,
                    ForwardChatMessageResponse.class);

            // check response status code
            if (response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody();
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
