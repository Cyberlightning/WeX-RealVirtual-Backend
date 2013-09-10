package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import com.cyberlightning.webserver.Application;
import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.ProfileService;

public class CoapSocket extends Thread implements IMessageEvent  {
	
	private DatagramSocket serverSocket;
	private ArrayList<DatagramPacket> sendBuffer= new ArrayList<DatagramPacket>();
	private ArrayList<DatagramPacket> receiveBuffer = new ArrayList<DatagramPacket>();
	
	public CoapSocket () {
		MessageService.getInstance().registerReceiver(this);
	}
	
	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(StaticResources.SERVER_PORT_COAP);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		
        
        while(true) {
        	
        	try {
				serverSocket.receive(receivedPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (receivedPacket.getData() != null) {
        		handleConnectedClient(receivedPacket);
        		MessageService.getInstance().broadcastCoapMessageEvent(receivedPacket);
        		receivedPacket = null; 
        	}
           
           if (!sendBuffer.isEmpty()) {
        	   try {
   				serverSocket.send(sendBuffer.get(sendBuffer.size()-1));
   				} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   				} 
        	   this.sendBuffer.remove(sendBuffer.size() - 1);
           }
           }
	}
	
    private  void handleConnectedClient(DatagramPacket _datagramPacket) {
			Client client = new Client(_datagramPacket.getAddress().getHostAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP);
			ProfileService.getInstance().registerClient(client);
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
