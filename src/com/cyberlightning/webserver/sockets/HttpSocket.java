package com.cyberlightning.webserver.sockets;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import com.cyberlightning.webserver.Application;
import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class HttpSocket extends Thread implements IMessageEvent{

private Socket clientSocket;
private ServerSocket serverSocket;
private BufferedReader socketReader;
private DataOutputStream outputStream;

public HttpSocket() {
	MessageService.getInstance().registerReceiver(this);
}

private void registerClient(Socket client) {
	this.clientSocket = client;
	System.out.println( "The Client "+ clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " is connected");
}

public Socket getClient() {
	return this.clientSocket;
}

@Override
public void run() {

	
	try {
		this.serverSocket = new ServerSocket (StaticResources.SERVER_PORT, StaticResources.MAX_CONNECTED_CLIENTS, InetAddress.getByName(StaticResources.LOCAL_HOST));
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
	while(true) {
		
		try {
			
			registerClient(serverSocket.accept());
			this.socketReader = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
			this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
			
			
			String clientRequest = socketReader.readLine();

			StringTokenizer tokenizer = new StringTokenizer(clientRequest);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			StringBuffer serverResponse = new StringBuffer(); //TODO
			
			
			
			if (httpMethod.equals("GET")) {
				
				if (httpQueryString.equals("/")) {
					// The default home page
					
					sendResponse(200, new File("../html/index.html").getAbsolutePath(), true); //absolute path for local dev
					
					
				} else {
					//This is interpreted as a file name
					String fileName = httpQueryString.replaceFirst("/", "");
					fileName = URLDecoder.decode(fileName);

					if (new File(fileName).isFile()){ //this is relative path
						sendResponse(200, fileName, true);
						
					}
					else {
						sendResponse(404, StaticResources.ERROR_404_MESSAGE, false);
					}
				}
				
			} else if (httpMethod.equals("POST")) { 
				
				String action = httpQueryString.replaceFirst("/", "");
				 
				if(action.equals("PREVIOUS")) {	
					MessageService.getInstance().broadcastHttpMessageEvent("Previous");
				}
				
				
			}else if (httpMethod.equals("PUT")) { 
				
				//String action = httpQueryString.replaceFirst("/", "");
				//TODO
				
			}else if (httpMethod.equals("DELETE")) { 
				
				//String action = httpQueryString.replaceFirst("/", "");
				//TODO
				
			} else {
				System.out.println(httpMethod.toString());
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}

public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {

	String statusLine = null;
	String serverdetails = StaticResources.SERVER_DETAILS;
	String contentLengthLine = null;
	String fileName = null;
	String contentTypeLine = "Content-Type: text/html" + "\r\n";
	FileInputStream fin = null;

	if (statusCode == 200)
		statusLine = "HTTP/1.1 200 OK" + "\r\n";
	else
		statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

	if (isFile) {
		fileName = responseString;
		fin = new FileInputStream(fileName);
		contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
		if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
			contentTypeLine = "Content-Type: \r\n";
	} else {
		//TODO responsestring
		
		contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
	}

	outputStream.writeBytes(statusLine);
	outputStream.writeBytes(serverdetails);
	outputStream.writeBytes(contentTypeLine);
	outputStream.writeBytes(contentLengthLine);
	outputStream.writeBytes("Connection: close\r\n");
	outputStream.writeBytes("\r\n");

	if (isFile) {
		sendFile(fin, outputStream);
	}
	else {
		outputStream.writeBytes(responseString);
	}
	
	outputStream.close();
}

public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
	
	byte[] buffer = new byte[1024] ;
	int bytesRead;

	while ((bytesRead = fin.read(buffer)) != -1 ) {
		out.write(buffer, 0, bytesRead);
	}

	fin.close();
}

@Override
public void httpMessageEvent(String msg) {
	// TODO Auto-generated method stub
	
}

@Override
public void coapMessageEvent(DatagramPacket _datagramPacket) {
	// TODO Auto-generated method stub
	
}

@Override
public void webSocketMessageEvent(String msg) {
	// TODO Auto-generated method stub
	
}


}

