package com.cyberlightning.webserver.sockets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.ProfileService;

public class CoapSocket implements Runnable,IMessageEvent  {
	
	private DatagramSocket serverSocket;
	private ArrayList<DatagramPacket> sendBuffer= new ArrayList<DatagramPacket>();
	private ArrayList<Client> baseStations = new ArrayList<Client>();
	private int port;
	
	public CoapSocket () {
		this(StaticResources.SERVER_PORT_COAP);
		
	}
	
	public CoapSocket (int _port) {
		this.port = _port;
	}
	
	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(this.port);
		this.serverSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		MessageService.getInstance().registerReceiver(this);	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		while(true) {
        	
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	
    		try {
        		
				serverSocket.receive(receivedPacket);
				System.out.print("Basestation packet received from " + receivedPacket.getAddress().getHostAddress());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (receivedPacket.getData() != null) {
        		handleConnectedClient(receivedPacket);
        		MessageService.getInstance().broadcastCoapMessageEvent(receivedPacket);
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
			Client client = new Client(_datagramPacket.getAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP);
			client.setType(Client.TYPE_BASESTATION);
			this.baseStations.add(client);
			//ProfileService.getInstance().registerClient(client);
	}
	
	@Override
	public void httpMessageEvent(String _address, String _msg) {
		
		JSONObject device = new JSONObject();
		device.put("DeviceID", "*");
		JSONObject root = new JSONObject();
		root.put("notificationURI", _address);
		root.put("request", _msg);
		root.put("contextEntities", device);
		
		
		byte[] b = new byte[1024];
		b = root.toJSONString().getBytes();
		
		DatagramPacket packet = new DatagramPacket(b, b.length, this.baseStations.get(0).getAddress(), this.baseStations.get(0).getPort());
		try {
			this.serverSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(_datagramPacket.getData()), Charset.forName("UTF-8"));
		try {
			StringBuilder str = new StringBuilder();
			for (int value; (value = input.read()) != -1; )
			    str.append((char) value);
			String s = str.toString();
			String d = str.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	@Override
	public void webSocketMessageEvent(String msg, String _address) {
		JSONObject device = new JSONObject();
		device.put("DeviceID", "*");
		JSONObject root = new JSONObject();
		root.put("notificationURI", _address);
		root.put("request", msg);
		root.put("contextEntities", device);
		
		System.out.print(root.toJSONString());
		byte[] b = new byte[1024];
		try {
			b = root.toJSONString().getBytes("UTF8");
			System.out.print(new String(b,"utf8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		DatagramPacket packet = new DatagramPacket(b, b.length, this.baseStations.get(0).getAddress(), this.baseStations.get(0).getPort());
		try {
			this.serverSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	
	
}
