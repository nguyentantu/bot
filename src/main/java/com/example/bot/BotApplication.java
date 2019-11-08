package com.example.bot;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotApplication {

	public static void main(String[] args) {
		//SpringApplication.run(BotApplication.class, args);

		try {
			String roomId = "169015857"; //roomId
			ChatworkClient chatworkClient = new ChatworkClient();
			chatworkClient.startChatBot(roomId);
			} catch (Exception ex) {
			ex.printStackTrace();
		}

//		try {
//			//String roomId = "169015857"; //roomId
//			ChatworkClient chatworkClient = new ChatworkClient();
//			for (String idRoom : chatworkClient.listRoom()) {
//				Thread thread = new Thread(new RunnableChatbot(idRoom));
//				thread.start();
//			}

	}

}
