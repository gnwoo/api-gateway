package com.gnwoo.apigateway;

import java.io.Serializable;

public class ForwardChatMessageResponse implements Serializable {
    private Long receiver_uuid;

    public ForwardChatMessageResponse(){}

    public ForwardChatMessageResponse(Long receiver_uuid){
        this.receiver_uuid = receiver_uuid;
    }

    public Long getReceiverUuid() {
        return receiver_uuid;
    }

    public void setReceiverUuid(Long receiver_uuid) {
        this.receiver_uuid = receiver_uuid;
    }
}
