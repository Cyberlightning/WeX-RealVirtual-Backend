package com.cyberlightning.webserver;

import java.net.DatagramPacket;


public class ProfileService implements MessageEvent{

	private static final ProfileService _profileService = new ProfileService();

	private ProfileService() {
		MessageHandler.getInstance().registerReceiver(this);
		
	}
	
	public static ProfileService getInstance() {
		return _profileService;
	}
	
	//@Override
	public void messageEvent(String _msg) {
		
		if (_msg.equals("Next")) {
			//TODO
		}
		
		if (_msg.equals("Previous")) {
			//TODO
		}
		
		
		//MessageHandler.getInstance().sendMessage(_msg);
	}

	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void udpMessageEvent(DatagramPacket _datagramPacket) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
