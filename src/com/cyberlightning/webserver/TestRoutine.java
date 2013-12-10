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
			String sample = "{\"d23c058698435eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"uT\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 15:02:39\",\"values\":[0.07543107122182846,-0.015922529622912407,0.01725415326654911]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"orientation\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}},{\"value\":{\"unit\":\"rads\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 15:02:39\",\"values\":[355.9173278808594,-85.8130111694336,4.165353775024414]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"gyroscope\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL Gyro\"}},{\"value\":{\"unit\":\"uT\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 15:02:39\",\"values\":[0.07543107122182846,-0.015922529622912407,0.01725415326654911]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"magneticfield\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 15:02:39\",\"values\":[352.0169982910156,-85.75300598144531,4.191023826599121]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"linearacceleration\",\"power\":1.5,\"vendor\":\"Google Inc.\",\"name\":\"Linear Acceleration Sensor\"}}],\"actuators\":[{\"configuration\":[{\"value\":\"100\",\"unit\":\"percent\",\"name\":\"viewsize\"}],\"actions\":[{\"value\":\"[marker1,marker2,marker3,marker4,marker6,marker7,marker8,marker9,marker10,marker11,marker12,marker13,marker14,marker15,marker15,marker16,marker17,marker18,marker19]\",\"primitive\":\"array\",\"unit\":\"string\",\"parameter\":\"viewstate\"}],\"callbacks\":[{\"target\":\"viewstate\",\"return_type\":\"boolean\"}],\"attributes\":{\"dimensions\":\"[480,800]\"}}],\"attributes\":{\"name\":\"Android device\"}}}}";
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