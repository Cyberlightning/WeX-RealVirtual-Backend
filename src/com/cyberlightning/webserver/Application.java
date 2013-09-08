package com.cyberlightning.webserver;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.cyberlightning.webserver.services.TranslationService;
import com.cyberlightning.webserver.sockets.CoapSocket;
import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;



public class Application  {




	public static void main(String[] args) throws Exception, IOException {
		
		
		@SuppressWarnings("unused")
		WebSocket webSocket = new WebSocket();
		webSocket.run();
		@SuppressWarnings("unused")
		HttpSocket httpSocket = new HttpSocket();
		httpSocket.run();
		@SuppressWarnings("unused")
		CoapSocket coapSocket = new CoapSocket();
		coapSocket.run();
	
		
	}
}
