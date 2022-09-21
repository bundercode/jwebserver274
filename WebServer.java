import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Brandon Smith
 * CS274 Fall 2013
 * Project 1
 * 
 */

public final class WebServer
{
	public static void main(String[] args) throws Exception {
		// Set the port number.
		int port = 6789;
		
		// Establish the listen socket.
		ServerSocket listener = new ServerSocket(port);
		
		// Process HTTP service requests in an infinite loop.
		while (true) {
			// Listen for a TCP connection request.
			Socket socket = listener.accept();
		
			// Construct an object to process the HTTP request message.
			HttpRequest request = new HttpRequest(socket);
		 
			// Create a new thread to process the request.
			Thread thread = new Thread(request);
		 
			// Start the thread.
			thread.start();
		}
	}
}

final class HttpRequest implements Runnable
{	
	final static String CRLF = "\r\n";
	Socket socket;
	
	// Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	
	// Implement the run() method of the Runnable interface.
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void processRequest() throws Exception {
		
		// Get a reference to the socket's input and output streams.
		InputStream is = this.socket.getInputStream();
		DataOutputStream os = new DataOutputStream(this.socket.getOutputStream());
		
		// Set up input stream filters.
		InputStreamReader isRead = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isRead);
		
		// Get the request line of the HTTP request message.
		String requestLine = br.readLine();
		
		// Display the request line.
		System.out.println();
		System.out.println(requestLine);
		
		// get and display the header lines.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		
		//Extract the filename from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken();  // skip over the method, which should be "GET"
		String fileName = tokens.nextToken();
		
		// Prepend a "." so that the file request is within the current directory.
		fileName = "." + fileName;
		
		// Default empty request to index.html
		//if (fileName.equals("./")) fileName = "./index.html";
		
		// Open the request file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}
		
		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) {
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-type: " +
						contentType(fileName) + CRLF;
		} else {
			statusLine = "HTTP/1.0 404 Not Found" + CRLF;
			contentTypeLine = "Content-type: " + 
						"text/html" + CRLF;
			entityBody = "<HTML>" +
					"<HEAD><TITLE>404: Page Not Found</TITLE></HEAD>" +
					"<BODY>404: Page Not Found</BODY></HTML>";
		}
		
		// Send the status line.
		os.writeBytes(statusLine);
		
		// Send the content type line.
		os.writeBytes(contentTypeLine);
		
		// Send a black line to indicate the end of the header lines.
		os.writeBytes(CRLF);
		
		// Send the entity body.
		if (fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else
			os.writeBytes(entityBody);
		
		// Close streams and socket.
		os.close();
		br.close();
		this.socket.close();
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os)
	throws Exception
	{
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		// Copy requested file into the socket's output stream.
		while((bytes = fis.read(buffer)) != -1){
			os.write(buffer, 0, bytes);
		}
	}
	
	private static String contentType(String fileName)
	{
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (fileName.endsWith(".gif")) {
			return "image/gif";
		}
		return "application/octet-stream";
	}
}
