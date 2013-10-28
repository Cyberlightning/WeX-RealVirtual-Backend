package com.cyberlightning.webserver.services;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.cyberlightning.webserver.interfaces.IMessageEvent;


public class MessageService {
	
	private static final MessageService _messageHandler = new MessageService();
	private List<IMessageEvent> registeredReceivers= new ArrayList<IMessageEvent>();

	private MessageService() {
		
	}
	
	public static MessageService getInstance () {
		return _messageHandler;
	}
	
	public void registerReceiver(IMessageEvent receiver) {
		this.registeredReceivers.add(receiver);
	}
	
	public void unregisterReceiver(IMessageEvent receiver) {
		
		ListIterator<IMessageEvent> i = this.registeredReceivers.listIterator(); //only ListIterator is allowed to remove or add from an collection of a contested resource or concurrentmodification exception is thrown
		while(i.hasNext()) {
			IMessageEvent ime = i.next();
			if(ime.equals(receiver)) {
				i.remove();
				break;
			}
		}
	}
	
	public void broadcastHttpMessageEvent(String _target, String msg) {
		
		for (IMessageEvent client : this.registeredReceivers) {
			client.httpMessageEvent(_target, msg);
		}
	}
	
	public void broadcastCoapMessageEvent(DatagramPacket _datagramPacket) {
		
		for (IMessageEvent client : this.registeredReceivers) {
			client.deviceMessageEvent(_datagramPacket);
		}
	}
	
	public void broadcastWebSocketMessageEvent(String msg, String address) {
		
		for (IMessageEvent client : this.registeredReceivers) {
			client.webSocketMessageEvent(msg, address);
		}
	}
}
