package com.cyberlightning.webserver.sockets;


	import java.io.*;  
import java.net.*;  
import java.security.*;  
import java.util.ArrayList;

import javax.xml.bind.*;  

import org.json.simple.JSONObject;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.TranslationService;
      
    public class WebSocket3 extends Thread implements IMessageEvent {  
          
        private ServerSocket server;  
        private Socket sock;  
        private InputStream in;  
        private OutputStream out;  
        private ArrayList<String> sendBuffer = new ArrayList<String>();
        public static final String SAMPLE_JSON = "{\"number\":1, \"value\":8},{\"number\":2, \"value\":16},{\"number\":3, \"value\":32},{\"number\":4, \"value\":64}";
        
        public WebSocket3() {  
        	MessageService.getInstance().registerReceiver(this);
        }  
        
        @Override 
        public void run() {
        	try {
				this.listen(StaticResources.WEB_SOCKET_PORT);
				this.handshake();
				 //String message = this.read();
				
				System.out.print(SAMPLE_JSON);
				
				
				//this.send(SAMPLE_JSON); 
				this.send(TranslationService.getJson().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }
        
//        /** throws Exception, because we don't really care much in this example */  
//        public void initialize() throws Exception {  
//            WebSocket ws = new WebSocket();  
//              
//            System.out.println("Listening...");  
//            ws.listen(StaticResources.WEB_SOCKET_PORT);  
//              
//            System.out.println("Handshake");  
//            ws.handshake();  
//              
//            System.out.println("Handshake complete!");  
//              
//            String message = ws.read();  
//            System.out.println("Message: " + message);  
//              
//            ws.send("I got your message! It's length was: " + message.length());  
//              
//            ws.close();  
//        }  
          
        public void listen(int port) throws IOException {  
      
            server = new ServerSocket(port);  
            sock = server.accept();  
            server.close();  
              
            in  = sock.getInputStream();  
            out = sock.getOutputStream();  
        }  
          
        private void handshake() throws Exception {  
      
            BufferedReader br = new BufferedReader(new  InputStreamReader(in,  "UTF8"));  
            PrintWriter    pw = new    PrintWriter(new OutputStreamWriter(out, "UTF8"));  
              
              
            // the first line of HTTP headers  
            String line = br.readLine();  
              
            if(!line.startsWith("GET"))  
                throw new IOException("Wrong header: " + line);  
              
            // we read header fields  
            String key = null;  
              
            // read line by line until we get empty line  
            while( !(line=br.readLine()).isEmpty() ) {  
                  
                if(line.toLowerCase().contains("sec-websocket-key")) {  
                    key = line.substring(line.indexOf(":")+1).trim();  
                }  
            }  
              
            if(key==null)  
                throw new IOException("No Websocket key specified");  
              
            System.out.println(key);  
              
            // add key and magic value  
            String accept = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";  
              
            // sha1  
            byte[] digest = MessageDigest.getInstance("SHA-1")  
                            .digest(accept.getBytes("UTF8"));  
            // and base64  
            accept = DatatypeConverter.printBase64Binary(digest);  
              
            // send http headers  
            pw.println("HTTP/1.1 101 Switching Protocols");  
            pw.println("Upgrade: websocket");  
            pw.println("Connection: Upgrade");  
            pw.println("Sec-WebSocket-Accept: " + accept);  
            pw.println();  
            pw.flush();  
              
        }  
          
        private void readFully(byte[] b) throws IOException {  
              
            int readen = 0;  
            while(readen<b.length)  
            {  
                int r = in.read(b, readen, b.length-readen);  
                if(r==-1)  
                    break;  
                readen+=r;  
            }  
        }  
          
        private String read() throws Exception {  
      
            int opcode = in.read();  
            boolean whole = (opcode & 0b10000000) !=0;  
            opcode = opcode & 0xF;  
              
            if(opcode!=1)  
                throw new IOException("Wrong opcode: " + opcode);  
              
            int len = in.read();  
            boolean encoded = (len >= 128);  
              
            if(encoded)  
                len -= 128;  
              
            if(len == 127) {  
                len = (in.read() << 16) | (in.read() << 8) | in.read();  
            }  
            else if(len == 126) {  
                len = (in.read() << 8) | in.read();  
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
          
        private void send(String message) throws Exception {  
              
            byte[] utf = message.getBytes("UTF8");  
              
            out.write(129);  
              
            if(utf.length > 65535) {  
                out.write(127);  
                out.write(utf.length >> 16);  
                out.write(utf.length >> 8);  
                out.write(utf.length);  
            }  
            else if(utf.length>125) {  
                out.write(126);  
                out.write(utf.length >> 8);  
                out.write(utf.length);  
            }  
            else {  
                out.write(utf.length);  
            }  
              
            out.write(utf);  
        }  
          
        private void close() {  
            try {  
                sock.close();  
            } catch (IOException e) {  
                System.err.println(e);  
            }  
        }
		@Override
		public void httpMessageEvent(String msg) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void coapMessageEvent(DatagramPacket _datagramPacket) {
			//this.sendBuffer.add(_datagramPacket.getData().toString());
			
			
			try {
				this.send(_datagramPacket.getData().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		@Override
		public void webSocketMessageEvent(String msg) {
			// TODO Auto-generated method stub
			
		}  
      
      
    }  






