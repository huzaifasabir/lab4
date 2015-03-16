

import java.io.*;
import java.net.*;
import java.util.*;

 public class htmlServer extends Thread {

     static final String HTMLs = "<html>" +"<title>HTTP POST Server in java</title>" +"<body>";
     
     static final String HTMLe = "</body>" + "</html>";
     
     Socket Clientconnector = null;
     bufferingedReader iFClient = null;
     DataOutputStream OTClient = null;

     
     public htmlServer(Socket client) {
         Clientconnector = client;
     }    
     
     public void run() {
 
       String currbuffering = null, boundary = null, fname = null, LContent = null;
       PrintWriter fout = null;
 
       try {
 
         System.out.println( "The Client "+
         Clientconnector.getInetAddress() + ":" + Clientconnector.getPort() + " is connected");
     
         iFClient = new bufferingedReader(new InputStreamReader (Clientconnector.getInputStream()));          
        OTClient = new DataOutputStream(Clientconnector.getOutputStream());
 
         currbuffering = iFClient.readLine();
         String headerLine = currbuffering;        
         StringTokenizer tokenizer = new StringTokenizer(headerLine);
         String httpMethod = tokenizer.nextToken();
         String httpQueryString = tokenizer.nextToken();
 
         System.out.println(currbuffering);
        
         if (httpMethod.equals("GET")) {
				if (httpQueryString.equals("/")) {
					String responseString = htmlServer.HTMLs +
					"<form action=\"http://127.0.0.1:4540\" enctype=\"multipart/form-data\"" +
					"method=\"post\">" +
					"Enter the name of the File <input name=\"file\" type=\"file\"><br>" +
					"<input value=\"Upload\" type=\"submit\"></form>" +
					"Upload only text files." +
					htmlServer.HTMLe;
					sendingRes(200, responseString , false);  
				} else {
					String fname = httpQueryString.replaceFirst("/", "");
					fname = URLDecoder.decode(fname);
					if (new File(fname).isFile()){
						sendingRes(200, fname, true);
					}
					else {
						sendingRes(404, "<b>Error 404 ...." , false);
					}
				}
         }
         else if (httpMethod.equals("HEAD")) {
				if (httpQueryString.equals("/")) {
					sendingRes(200, currbuffering.toString(), false);
				} else {
					String fname = httpQueryString.replaceFirst("/", "");
					fname = URLDecoder.decode(fname);
					if (new File(fname).isFile()){
						headsendingRes(200, fname, true);
					}
					else {
						sendingRes(404, "<b>Error 404 ....", false);
					}
				}
         }
         else { 
             System.out.println("POST request");
            do {
                 currbuffering = iFClient.readLine();
                                     
                 if (currbuffering.indexOf("Content-Type: multipart/form-data") != -1) {
                   String boundary = currbuffering.split("boundary=")[1];                        
           
                   while (true) {
                       currbuffering = iFClient.readLine();
                       if (currbuffering.indexOf("Content-Length:") != -1) {
                           LContent = currbuffering.split(" ")[1];
                           break;
                       }              
                   }
           
                 
           
                   while (true) {
                       currbuffering = iFClient.readLine();
                       if (currbuffering.indexOf("--" + boundary) != -1) {
                           fname = iFClient.readLine().split("fname=")[1].replaceAll("\"", "");                                
                           String [] filelist = fname.split("\\" + System.getProperty("file.separator"));
                          fname = filelist[filelist.length - 1];                  
                           System.out.println("File to be uploaded = " + fname);
                           break;
                       }              
                   }
           
                   String fileContentType = iFClient.readLine().split(" ")[1];
           
                  iFClient.readLine();
           
                   fout = new PrintWriter(fname);
                   String prevLine = iFClient.readLine();
                   currbuffering = iFClient.readLine();      
          
                   while (true) {
                       if (currbuffering.equals("--" + boundary + "--")) {
                           fout.print(prevLine);
                           break;
                       }
                       else {
                           fout.println(prevLine);
                       }
                       prevLine = currbuffering;              
                       currbuffering = iFClient.readLine();
                  }
           
                   sendingRes(200, "File " + fname + " Uploaded..", false);
                   fout.close();           
                 }                                               
             }while (iFClient.ready());
           }
       } catch (Exception e) {
             e.printStackTrace();
       }
     }
 	public void headsendingRes (int statusCode, String responseString, boolean isFile) throws Exception {

		String lineSta = null;
		String servdet = "Server: Java HTTPServer";
		String LContentLine = null;
		String fname = null;
		String contentLineType = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;

		if (statusCode == 200)
			lineSta = "HTTP/1.1 200 OK" + "\r\n";
		else
			lineSta = "HTTP/1.1 404 Not Found" + "\r\n";

		if (isFile) {
			fname = responseString;
			fin = new FileInputStream(fname);
			LContentLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fname.endsWith(".htm") && !fname.endsWith(".html"))
				contentLineType = "Content-Type: \r\n";
		}
		else {
			responseString = myHTTPServer.HTMLs + responseString + myHTTPServer.HTMLe;
			LContentLine = "Content-Length: " + responseString.length() + "\r\n";
		}

		OTClient.writeBytes(lineSta);
		OTClient.writeBytes(servdet);
		OTClient.writeBytes(contentLineType);
		OTClient.writeBytes(LContentLine);
		OTClient.writeBytes("Connection: close\r\n");
		OTClient.writeBytes("\r\n");

		OTClient.writeBytes(responseString);

		OTClient.close();
	}
     public void sendingRes (int statusCode, String responseString, boolean isFile) throws Exception {
 
         String lineSta = null;
         String servdet = "Server: Java HTTPServer";
         String LContentLine = null;
         String fname = null;
         String contentLineType = "Content-Type: text/html" + "\r\n";
         FileInputStream fin = null;
 
         if (statusCode == 200)
             lineSta = "HTTP/1.1 200 OK" + "\r\n";
         else
             lineSta = "HTTP/1.1 404 Not Found" + "\r\n";
     
         if (isFile) {
             fname = responseString;    
            fin = new FileInputStream(fname);
             LContentLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
             if (!fname.endsWith(".htm") && !fname.endsWith(".html"))
                 contentLineType = "Content-Type: \r\n";
         }                
         else {
             responseString = htmlServer.HTMLs + responseString + htmlServer.HTMLe;
            LContentLine = "Content-Length: " + responseString.length() + "\r\n";
       }    
  
         OTClient.writeBytes(lineSta);
         OTClient.writeBytes(servdet);
         OTClient.writeBytes(contentLineType);
         OTClient.writeBytes(LContentLine);
         OTClient.writeBytes("Connection: close\r\n");
         OTClient.writeBytes("\r\n");

        if (isFile) FileRead(fin, OTClient);
         else OTClient.writeBytes(responseString);
 
         OTClient.close();
     }

     public void FileRead (FileInputStream f, DataOutputStream out) throws Exception {
         byte[] buffering = new byte[1024] ;
         int readingBytes;

         while ((readingBytes = f.read(buffering)) != -1 ) {
         out.write(buffering, 0, readingBytes);
         }
         f.close();
     }
     
    public static void main (String args[]) throws Exception {
 
         ServerSocket Server = new ServerSocket (4540, 10, InetAddress.getByName("127.0.0.1")); 
                         
         while(true) {                                 
                 Socket connected = Server.accept();
                 (new htmlServer(connected)).start();
         }
     }
 }
