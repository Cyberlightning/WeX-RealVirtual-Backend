package com.cyberlightning.webserver.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class WebSocketWorker implements Runnable {

	private Socket clientSocket;
	private String serverResponse = new String();
	private InputStream input;
	private OutputStream output;
	private Thread sendWorker;
	
	private volatile boolean isConnected = true;

	public final String uuid = UUID.randomUUID().toString();
	public final int type =  StaticResources.TCP_CLIENT;
	
	public static final String WEB_SOCKET_SERVER_RESPONSE = 
			"HTTP/1.1 101 Switching Protocols\r\n"	+
			"Upgrade: websocket\r\n"	+
			"Connection: Upgrade\r\n" +
			"Sec-WebSocket-Accept: ";

	/**
	 * 	
	 * @param _parent
	 * @param _client
	 */
	public WebSocketWorker (Socket _client) {
		this.clientSocket = _client;
	}
	
	/**
	 * Handles handshaking between connecting client and server
	 */
	private void initialize() {
		try {
			this.input = this.clientSocket.getInputStream();
			this.output = this.clientSocket.getOutputStream();
			System.out.println("new client attempting connection");
			BufferedReader inboundBuffer= new BufferedReader(new InputStreamReader(this.input));
			DataOutputStream outboundBuffer = new DataOutputStream(this.output);
			
			String line;
			while( !(line=inboundBuffer.readLine()).isEmpty() ) {  
				 parseRequestLine(line);                 
			}  
			
			outboundBuffer.writeBytes(this.serverResponse);
			outboundBuffer.flush();
			
			System.out.println("Handshake complete");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the client request for security key and generates header for server response to complete the handshake
	 * @param _request
	 */
	private void parseRequestLine(String _request)  {
		System.out.println("CLIENT REQUEST: " +_request);
		if (_request.contains("Sec-WebSocket-Key: ")) {
			this.serverResponse = WEB_SOCKET_SERVER_RESPONSE + generateSecurityKeyAccept(_request.replace("Sec-WebSocket-Key: ", "")) + "\r\n\r\n";
		} 
	}
	
	/**
	 * Generates security key for the session using magic string and generated key.
	 * @param _secKey Client security key
	 * @return Server security key
	 */
	private String generateSecurityKeyAccept (String _secKey) {
		
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] secKeyByte = (_secKey + StaticResources.MAGIC_STRING).getBytes();
			secKeyByte = sha1.digest(secKeyByte);
			_secKey = Base64.encodeBase64String(secKeyByte);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return _secKey;
	}
	
	@Override
	public void run() {
	
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_CONNECTED);
		this.initialize();
		Runnable runnable = new SendWorker();
		this.sendWorker = new Thread(runnable);
		this.sendWorker.start();
	
		while(this.isConnected) {
			
			try {
				if(this.input.available() > 0) {
					
					int opcode = this.input.read();  
				    @SuppressWarnings("unused")
					boolean whole = (opcode & 0b10000000) !=0;  
				    opcode = opcode & 0xF;
				    
				    if (opcode != 8) { 
				    	 
				    	handleClientMessage(read());
				    	 
				    } else {
				    	 
					    /*|Opcode  | Meaning                             | Reference |
					     -+--------+-------------------------------------+-----------|
					      | 0      | Continuation Frame                  | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 1      | Text Frame                          | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 2      | Binary Frame                        | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 8      | Connection Close Frame              | RFC 6455  |
				     	 -+--------+-------------------------------------+-----------|
					      | 9      | Ping Frame                          | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 10     | Pong Frame                          | RFC 6455  |*/
				    	 
				    	System.out.println("Client message type: " + opcode);
				    	this.closeSocketGracefully();
				    }
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Connecttion interrupted: " + e.getLocalizedMessage());
				this.closeSocketGracefully();
			}
		}
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_DISCONNECTED);
		return;	//Exits thread
	}
	
	/**
	 * 
	 * @param _msg
	 */
	private void handleClientMessage(String _msg) { //TODO design post method options
		
		String[] queries = _msg.split("&");
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
						//sendResponse(SimulateSensorResponse.uploadFile(file));
					} 
				}
				
			}
				
			MessageService.getInstance().messageBuffer.put(id, value);
	}
	
	/**
	 * 
	 */
	private void closeSocketGracefully() {
		try {	
			this.input.close();
			this.clientSocket.close();
			this.isConnected = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param b
	 * @throws IOException
	 */
	private void readFully(byte[] b) throws IOException {  
        
        int readen = 0;  
        while(readen<b.length)  
        {  
            int r = this.input.read(b, readen, b.length-readen);  
            if(r==-1)  
                break;  
            readen+=r;  
        }  
    } 
	
    /**
     *  
     * @return
     * @throws Exception
     */
    private String read() throws Exception {  
          
        int len = this.input.read();  
        boolean encoded = (len >= 128);  
          
        if(encoded)  
            len -= 128;  
          
        if(len == 127) {  
            len = (this.input.read() << 16) | (this.input.read() << 8) | this.input.read();  
        }  
        else if(len == 126) {  
            len = (this.input.read() << 8) | this.input.read();  
        }  
          
        byte[] key = null;  
          
        if(encoded) {  
            key = new byte[4];  
            readFully(key);  
        }  
          
        byte[] frame = new byte[len];  
          
        readFully(frame);  
          
        if(encoded) {  
            for(int i=0; i<frame.length; i++) {  
                frame[i] = (byte) (frame[i] ^ key[i%4]);  
            }  
        }  
          
        return new String(frame, "UTF8");  
    }
    
  
	private class SendWorker implements Runnable,IMessageEvent {
		
		
		private  Map<Integer, Object> sendBuffer = new ConcurrentHashMap<Integer, Object>(); 
		
		
		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this,uuid);
			
			while (isConnected) {
				if (sendBuffer.isEmpty()) continue;
				
				try {
					
					Iterator<Integer> i = this.sendBuffer.keySet().iterator();
		     		while(i.hasNext()) {
		     			
		     			int key = i.next();
		     			if (sendBuffer.get(key) instanceof DatagramPacket) {
		     					String _content = new String(((DatagramPacket)sendBuffer.get(key)).getData(), "utf8");
		     					this.send(_content);
		     			} else if (sendBuffer.get(key) instanceof String) {
		     					this.send((String)sendBuffer.get(key));
		     			}
		     			sendBuffer.remove(key);
			        	
		     		}
		     		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}      
			}
			MessageService.getInstance().unregisterReceiver(uuid);
			return;
		}
		
		/**
		 * 
		 * @param message
		 * @throws Exception
		 */
		private void send(String message) throws Exception {  
	          
	        byte[] utf = message.getBytes("UTF8");  
	          
	        output.write(129);  
	          
	        if(utf.length > 65535) {  
	        output.write(127);  
	        output.write(utf.length >> 16);  
	        output.write(utf.length >> 8);  
	        output.write(utf.length);  
	        }  
	        else if(utf.length>125) {  
	        output.write(126);  
	        output.write(utf.length >> 8);  
	        output.write(utf.length);  
	        }  
	        else {  
	        output.write(utf.length);  
	        }  
	          
	        output.write(utf);

	    } 
		
		/**
		 * 		
		 */
		@Override
		public void onMessageReceived(int _type, Object _msg) {
			((ConcurrentHashMap<Integer, Object>) this.sendBuffer).putIfAbsent(_type, _msg);
		}

	}
	

}
