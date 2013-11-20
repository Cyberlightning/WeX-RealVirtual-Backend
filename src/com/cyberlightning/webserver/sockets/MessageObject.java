package com.cyberlightning.webserver.sockets;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MessageObject  {
	
	public Object payload ;
	public int port;
	public int originType;
	public String responseCode;
	public String originUUID;
	public String targetUUID;
	public ArrayList<InetSocketAddress> targetAddresses;
	
	
	
	public MessageObject(String _origin, int _originType, String _statusCode){
		this.responseCode = _statusCode;
		this.originUUID = _origin;
		this.originType = _originType;
	}
	
	public MessageObject(String _uuid, int _originType, Object _payload){
		this.payload = _payload;
		this.originUUID = _uuid;
		this.originType = _originType;
	}
	
	public MessageObject(String _uuid, int _originType, ArrayList<InetSocketAddress> _targetAddresses, Object _payload){
		this.payload = _payload;
		this.originUUID = _uuid;
		this.targetAddresses = _targetAddresses;
		this.originType = _originType;
	}
	
	
}
