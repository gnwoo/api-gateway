package com.gnwoo.apigateway;

public class ChatMessage {
    private String senderUsername;
    private String receiverUsername;
    private String message;

    public ChatMessage(){}

    public ChatMessage(String userName, String receiverUsername, String message) {
        this.senderUsername = userName;
        this.message = message;
        this.receiverUsername = receiverUsername;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new String("[sender]" + this.getSenderUsername() +
                " [send message]" + this.getMessage() +
                " [to receiver]" + this.getReceiverUsername() + ".");
    }
}
