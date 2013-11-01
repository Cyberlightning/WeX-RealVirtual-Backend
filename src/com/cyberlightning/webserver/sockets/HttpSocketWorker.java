package com.cyberlightning.webserver.sockets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.SimulateSensorResponse;
import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.MessageHeader;
import com.cyberlightning.webserver.entities.SpatialQuery;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.DataStorageService;
import com.cyberlightning.webserver.services.MessageService;

public class HttpSocketWorker implements Runnable,IMessageEvent {

	private DataOutputStream output;
	private MessageHeader header;
	private Map<Integer, String> receivedQueries = new ConcurrentHashMap<Integer, String>(); 
	private Socket clientSocket;
	
	private volatile boolean isConnected = true;
	public final String uuid = UUID.randomUUID().toString();
	public final int type = StaticResources.HTTP_CLIENT;
	
	
	public HttpSocketWorker(Socket _socket) {
		this.clientSocket = _socket;
		MessageService.getInstance().registerReceiver(this,this.uuid);
	}
	
	@Override
	public void run() {
		
		while(isConnected) {
			
			try {
				
				InputStream in = clientSocket.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				this.output = new DataOutputStream(clientSocket.getOutputStream());
				String request = "";
				
				byte[] buffer = new byte[4096];
				int len = in.read(buffer);
				bos.write(buffer, 0, len);
				request = new String(bos.toByteArray(),"utf8");
				
				String[] result = request.split("\n");
				int fromIndex =  result[0].indexOf("?");
				int toIndex = result[0].indexOf("HTTP");
				
				/* Passes the urlencoded query string to appropriate http method handlers*/
				if (result[0].trim().toUpperCase().contains("GET")) {
					this.handleGETMethod(result[0].substring(fromIndex + 1, toIndex).trim());
				
				}
				else if (result[0].trim().toUpperCase().contains("POST")) {
					
					fromIndex =  result[0].indexOf("/");
					String content = result[0].substring(fromIndex, toIndex);
					if (content.trim().contentEquals("/")) {
						this.handlePOSTMethod(result[result.length-1].toString(), false);
					} else {
						this.handlePOSTMethod(content, true);
					}
					
				}
				else if (result[0].trim().toUpperCase().contains("PUT")) {
					this.handlePUTMethod(result[result.length-1].toString());
				}
				else if (result[0].trim().toUpperCase().contains("DELETE")) {
					this.handleDELETEMethod(result[result.length-1].toString());
				}
				else System.out.println(result[0].trim().toUpperCase());
		
			} catch (Exception e) {
				e.printStackTrace();
				System.out.print(e.getLocalizedMessage());
			} 	
		}
		return; //Exit thread
	}

	private void sendResponse(String _content) {
		
		String statusLine = "HTTP/1.1 200 OK" + "\r\n";
		String contentTypeLine = "Content-Type: text/plain; charset=utf-8" + "\r\n";
		String connectionLine = "Connection: close\r\n";
		String contentLengthLine = "Content-Length: " + _content.length();
		String contentLine = _content;
		
		try {	
			this.output.writeBytes(statusLine);;
			this.output.writeBytes(contentTypeLine);
			this.output.writeBytes(contentLengthLine);
			this.output.writeBytes(connectionLine);
			this.output.writeBytes("\r\n");
			this.output.writeBytes(contentLine);

			this.output.close(); //client connection will be kept alive untill response is send
			this.isConnected = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void handlePUTMethod(String _request) {
		//TODO handPUTMethod
	}

	private void handleDELETEMethod(String _request) {
		//TODO handDELETEMethod
	}
	@Override
	public void onMessageReceived(int _type, Object _msg) {
		// TODO Auto-generated method stub
		
	}
	private void handleGETMethod(String _content) {
		
		String[] queries = _content.split("&");
		
		for (int i = 0; i < queries.length; i++) {
			if(queries[i].contains("action")) {
				String[] action = queries[i].split("=");
				if (action[1].contentEquals("loadById")) {
					for (int j = 0; j < queries.length;j++) {
						if (queries[j].contains("device_id")) {
							String[] device = queries[j].split("=");
							sendResponse(DataStorageService.getInstance().getEntryById(device[1]));
						}
					}
					
				} else if (action[1].contentEquals("loadBySpatial")) {
					String lat = "";
					String lon = "";
					int radius = 0;
					
					for (int j = 0; j < queries.length;j++) {
						
						if (queries[j].contains("lat")) {
							String[] la = queries[j].split("=");
							lat = la[1].trim();
						}
						if (queries[j].contains("lon")) {
							String[] lo = queries[j].split("=");
							lon = lo[1].trim();
						}
						if (queries[j].contains("radius")) {
							String[] rad = queries[j].split("=");
							radius = Integer.parseInt(rad[1].trim());
						}
					}
				
					sendResponse(DataStorageService.getInstance().getEntriesByParameter(new SpatialQuery(Float.parseFloat(lat),Float.parseFloat(lon),radius,10)));
				} 
			}
		}
		//header.setSenderAddress(clientSocket.getInetAddress().getHostAddress());
		//MessageService.getInstance().messageBuffer.put(header, _content); //TODO resolve content

	}

	private void handlePOSTMethod(String _content, boolean _isFile) {
		
		String[] queries = _content.split("&");
		String id = "";
		String actuator = "";
		String parameter = "";
		String value = "";
		
		for (int i = 0; i < queries.length; i++) {
			
			if(queries[i].contains("action")) {
				String[] action = queries[i].split("=");
				
				if (action[1].contentEquals("update")) {
				
					for (int j = 0; j < queries.length; j++) {
						
						if (queries[j].contains("device_id")) {
							String[] s = queries.clone()[j].trim().split("=");
							id = s[1];
						} else if (queries[j].contains("actuator")){
							String[] s = queries.clone()[j].trim().split("=");
							actuator = s[1];
						} else if (queries[j].contains("parameter")) {
							String[] s = queries.clone()[j].trim().split("=");
							parameter = s[1];
						} else if (queries[j].contains("value")) {
							String[] s = queries.clone()[j].trim().split("=");
							value = s[1];
						}
					}
					
					
					
				}else if (action[1].contentEquals("upload")) {
					
					File file = new File("marker.bmp");
					sendResponse(SimulateSensorResponse.uploadFile(file));
				} 
			}
			
		}
		//this.sendResponse(SimulateSensorResponse.updateActuator(id,actuator,parameter,value));
		header.setSenderAddress(clientSocket.getInetAddress().getHostAddress());
		MessageService.getInstance().messageBuffer.put(header, value);
		

	}

	/*
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
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		}

		this.output.writeBytes(statusLine);
		this.output.writeBytes(serverdetails);
		this.output.writeBytes(contentTypeLine);
		this.output.writeBytes(contentLengthLine);
		this.output.writeBytes("Connection: close\r\n");
		this.output.writeBytes("\r\n");

		if (isFile) {
			sendFile(fin, this.output);
		}
		else {
			this.output.writeBytes(responseString);
		}
		
		this.output.close();
	}

	public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
		
		byte[] buffer = new byte[1024] ;
		int bytesRead;

		while ((bytesRead = fin.read(buffer)) != -1 ) {
			out.write(buffer, 0, bytesRead);
		}

		fin.close();
	}
	*/





	
	
	
}

