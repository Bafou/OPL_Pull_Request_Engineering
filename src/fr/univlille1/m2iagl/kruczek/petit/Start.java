package fr.univlille1.m2iagl.kruczek.petit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Start {


	public static void main(String[] args)  {
		HttpServer server=null;
		try {
			server = HttpServer.create(new InetSocketAddress(8000), 0);
		} catch (IOException e) {
			System.out.println("impossible to create httpServer");
			e.printStackTrace();
		}
		server.createContext("/github", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException  {
			
			String bodyString=null;
			try {
				bodyString = getBody(t);
			} catch (IOException e) {
				System.out.println("impossible to get body request");
				e.printStackTrace();
			}
			//System.out.println(bodyString);
			JSONObject obj = new JSONObject(bodyString);
			
			String pullUrl = obj.getJSONObject("pull_request").getString("url");
			String diffUrl = obj.getJSONObject("pull_request").getString("diff_url");
			System.out.println(pullUrl);
			System.out.println(diffUrl);
			
			URL yahoo = new URL(diffUrl);
			URLConnection yc = yahoo.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;

			boolean javaLine=false;
			while ((inputLine = in.readLine()) != null) {
				
				
				if(inputLine.startsWith("diff --git") && inputLine.endsWith(".java")){
					javaLine=true;
					System.out.println(inputLine);
				}else if(inputLine.startsWith("diff --git") && !inputLine.endsWith(".java")){
					javaLine=false;
					System.out.println();
				}
				
				if(javaLine==true){
					if(inputLine.startsWith("+") && !inputLine.startsWith("+++")){
						System.out.println(inputLine);
					}
				}
			}
			in.close();

			String response = "This is the response";
			t.sendResponseHeaders(202, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		
		
		public String getBody(HttpExchange h) throws IOException{
			InputStreamReader isr=null;
		
				isr = new InputStreamReader(h.getRequestBody(),"utf-8");

			BufferedReader br = new BufferedReader(isr);
			// From now on, the right way of moving from bytes to utf-8 characters;
			int b;
			StringBuilder buf = new StringBuilder();
		
				while ((b = br.read()) != -1) {
				    buf.append((char) b);
				}

			
				br.close();
				isr.close();
			
			
			//System.out.println(buf);
			return buf.toString();
		}
	}

	
}
