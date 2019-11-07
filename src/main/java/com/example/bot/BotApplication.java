package com.example.bot;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotApplication {

	public static void main(String[] args) {
		//SpringApplication.run(BotApplication.class, args);
		try {
			String roomId = "168043542"; // Replace your roomId
			ChatworkClient chatworkClient = new ChatworkClient();
			chatworkClient.startChatBot(roomId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
