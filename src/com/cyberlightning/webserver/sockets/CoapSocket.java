package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.DataStorageService;

public class CoapSocket implements Runnable  {
	
	private DatagramSocket serverSocket;
	private int port;
	
	public CoapSocket () {
		this(StaticResources.SERVER_PORT_COAP);
	}
	
	public CoapSocket (int _port) {
		this.port = _port;
		
		Runnable sendWorker = new SendWorker();
		Thread t = new Thread(sendWorker);
		t.start();
	}
	
	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(this.port);
		this.serverSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		while(true) {
        	
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	
    		try {
        		
				serverSocket.receive(receivedPacket);
				System.out.println("Basestation packet received from " + receivedPacket.getAddress().getHostAddress());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		DataStorageService.getInstance().addEntry(StaticResources.getTimeStamp(), receivedPacket);
    		//MessageService.getInstance().broadcastCoapMessageEvent(receivedPacket);  //TODO remove once put in SerializationService
           
          
		}
	}
	
	private class SendWorker implements Runnable,IMessageEvent {
		
		public  Map<String, DatagramPacket> sendBuffer = new ConcurrentHashMap<String, DatagramPacket>(100); 
		

		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this);
			
			while (true) {
				 
				if (!sendBuffer.isEmpty()) {
		        	   
		        	   try {
		        		
		        		   Iterator<String> i = sendBuffer.keySet().iterator();
		        		
		        		   while(i.hasNext()) {
		        			   String key = i.next();
		        			   //byte[] b = this.formatJSON(sendBuffer.get(key));
		        			   //DatagramPacket packet = new DatagramPacket(b, b.length, "ds", 23);
		        			
			        			serverSocket.send(sendBuffer.get(key));
			        			sendBuffer.remove(key);
		        		   }
		   				
		   				} catch (IOException e) {
		   					// TODO Auto-generated catch block
		   					e.printStackTrace();
		   					break;
		   				} 
		           }
			}
			
		}
		
		@Override
		public void httpMessageEvent(String address, String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deviceMessageEvent(DatagramPacket _datagramPacket) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void webSocketMessageEvent(String msg, String address) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
   
    
    
    
	
	
//	private byte[] formatJSON(String _msg) {
//    	
//    	JSONObject device = new JSONObject();
//		device.put("DeviceID", "*");
//		JSONObject root = new JSONObject();
//		//root.put("notificationURI", _address);
//		root.put("request", _msg);
//		root.put("contextEntities", device);
//	
//		byte[] b = new byte[1024];
//		try {
//			b = root.toJSONString().getBytes("UTF-8");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			System.out.println("UnsupportedEncodingException: " + e1.getMessage());
//		}
//		
//		return b;
//    }
    
//    private void sendToClients(String _msg, String _address) {
//    	
//    	Iterator<Client> i = this.baseStations.iterator();
//    	byte[] b = this.formatJSON(_msg, _address);
//		
//
//    	while (i.hasNext()) {
//    		
//    		Client client = i.next();
//    		DatagramPacket packet = new DatagramPacket(b, b.length, client.getAddress() , client.getPort());
//    		
//    		try {
//    			this.serverSocket.send(packet);
//    			System.out.println("Packet send to basestation :" + client.getAddress().getHostAddress());
//    		} catch (IOException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    			System.out.println("IOException: " + e.getMessage());
//    		}
//    	}
//    }
//	 private boolean handleConnectedClient(DatagramPacket _datagramPacket ) {
//	    	
//	    	Iterator<Client> i = this.baseStations.iterator();
//	    	boolean isRegistered = false;
//	    	while (i.hasNext()) {
//	    		if (i.next().getAddress().getHostAddress().contentEquals(_datagramPacket.getAddress().getHostAddress())) {
//	    			isRegistered = true;
//	    		}
//	    	}
//	    	if (!isRegistered) {
//	    		this.baseStations.add(new Client(_datagramPacket.getAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP, Client.TYPE_BASESTATION));
//	    	}
//	    	return isRegistered;
//	    }
	
	
}
