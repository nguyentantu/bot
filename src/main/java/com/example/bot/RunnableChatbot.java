package com.example.bot;

public class RunnableChatbot implements Runnable {
    ChatworkClient chatworkClient;
    String cc;

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            chatworkClient.startChatBot(cc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RunnableChatbot(String idRoom) {
        cc = idRoom;
        chatworkClient = new ChatworkClient();
    }
}
