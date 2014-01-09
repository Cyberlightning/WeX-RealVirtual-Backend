package com.cyberlightning.webserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.cyberlightning.webserver.services.Gzip;


public class TestRoutine implements Runnable {

	
	private DatagramSocket testSocket;
	@Override
	public void run() {
		
		try {
			this.testSocket = new DatagramSocket(StaticResources.SERVER_PORT_COAP+ 1);
			this.testSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to create test socket: " + e.getMessage());
			return;
		}
		
		Runnable receiveRoutine = new TestReceiver();
		Thread t = new Thread(receiveRoutine);
		t.start();
		
		while (true) {
		
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			
			}
			String sample = "{\"d23c058698435eff\":{\"d23c058698435eff\":{\"sensors\":[{\"configuration\":[{\"interval\":1000,\"unit\":\"ms\"}],\"value\":{\"unit\":\"meters in second\",\"primitive\":\"double\",\"time\":\"2014-01-09 12:44:56\",\"values\":\"0.51\"},\"attributes\":{\"type\":\"anemometer\",\"voltage\":\"5v\",\"vendor\":\"Modern Device\",\"name\":\"wind speed meter\"}}],\"actuators\":[{\"configuration\":[],\"actions\":[{\"value\":\"[true,false]\",\"primitive\":\"Boolean\",\"unit\":\"boolean\",\"parameter\":\"powerstate\"}],\"callbacks\":[{\"target\":\"powerstate\",\"return_type\":\"Boolean\"}],\"attributes\":{\"type\":\"powersocket\",\"name\":\"IQSW-IP 10 IQSocket\"}}],\"attributes\":{\"location\":\"Cyber development\",\"gps\":[65.0117870432389,25.47571696359944],\"name\":\"CyberDruido\"}}}}";
			byte[] byteBuffer = null;
			try {
				byteBuffer = Gzip.compress(sample);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			
			DatagramPacket testPacket = null;
			
			try {
				testPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getLocalHost(),StaticResources.SERVER_PORT_COAP);
				
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Test Packet sending failed: " + e1.getMessage());
			}
			
			try {
				this.testSocket.send(testPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Test Packet sending failed: " + e.getMessage());
			}
			System.out.println("Test Packet send to " + testPacket.getAddress().getHostAddress() + ":" + testPacket.getPort() );
		}
	}
	
	public class TestReceiver implements Runnable {
		
		
		@Override
		public void run() {
				while (true) {
				
			
				
				byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
	    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
	        	
	    		try {
	        		testSocket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error in receiving a packet:" + e.getMessage());
					break;
				}
	    		String payload = "";
	    		try {
					payload  = new String(receivedPacket.getData(),"utf8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Error unpacking packet received from server:" + e.getMessage()); 
				}
	    		System.out.println("Packet received from server:" + payload);
			}
			return;
		}
	
	}
	
}