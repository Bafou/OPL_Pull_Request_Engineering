package fr.univlille1.m2iagl.kruczek.petit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Start {
	
	public static String token = "";// Antoine token = 97174177adc084e5329cd1b4adb8bf4777bb2a79

	public static void main(String[] args) throws IOException {
		System.out.println("Welcome in initiation of the check pull request server : ");
		System.out.println("Please enter the token needed to write a comment : ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		token = br.readLine();
		System.out.println("Your token is taken in account, waiting for webhook");
		
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(8000), 0);
		} catch (final IOException e) {
			System.out.println("impossible to create httpServer");
			e.printStackTrace();
		}
		server.createContext("/github", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	static class MyHandler implements HttpHandler {
		
		private static final String USER_AGENT = "Check-Pull-Request";
		
		public void handle(final HttpExchange httpExchange) throws IOException {

			
			String bodyString = null;
			try {
				bodyString = getBody(httpExchange);
			} catch (final IOException e) {
				System.out.println("impossible to get body request");
				e.printStackTrace();
			}

			JSONObject obj = new JSONObject(bodyString);
			if (obj.has("pull_request")) {

				System.out.println("Receiving a new pull request event : ");
				final String pullUrl = obj.getJSONObject("pull_request").getString("url");
				final String diffUrl = obj.getJSONObject("pull_request").getString("diff_url");
				final String issueUrl = obj.getJSONObject("pull_request").getString("issue_url");
				final String pullState = obj.getJSONObject("pull_request").getString("state");
				System.out.println("\tPull URL : " + pullUrl);
				System.out.println("\tDiff URL : " + diffUrl);
				System.out.println("\tState : " + pullState);

				if (!pullState.equals("closed")) {
					final URL yahoo = new URL(diffUrl);
					final URLConnection yc = yahoo.openConnection();
					final BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
					String inputLine;

					boolean javaLine = false;
					while ((inputLine = in.readLine()) != null) {

						if (inputLine.startsWith("diff --git") && inputLine.endsWith(".java")) {
							javaLine = true;
							System.out.println(inputLine);
						} else if (inputLine.startsWith("diff --git") && !inputLine.endsWith(".java")) {
							javaLine = false;
							System.out.println();
						}

						if (javaLine == true) {
							if (inputLine.startsWith("+") && !inputLine.startsWith("+++")) {
								System.out.println(inputLine);
							}
						}
					}
					in.close();

					String response = "This is the response";

					httpExchange.sendResponseHeaders(202, response.length());
					OutputStream os = httpExchange.getResponseBody();
					os.write(response.getBytes());
					os.close();
					commentPR(issueUrl, Start.token);
				} else {
					System.out.println("The pull request is closed, no need to read");
				}
			}
		}

		public static void commentPR(final String issueUrl, final String token) {
			System.out.println("Writing the comment");
			final String stringUrl = issueUrl+ "/comments";
			System.out.println("Write to url : " + stringUrl);

			try {
				final URL url = new URL(stringUrl);
				final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Authorization", "token " + token);
				con.setRequestProperty("content-type", "application/json; charset=UTF-8");
				con.setDoOutput(true);
				
				final JSONObject bodyResponse = new JSONObject();
				bodyResponse.put("body", "OK : This Pull Request has a good code style");
				final OutputStream os = con.getOutputStream();
				System.out.println("Sending message : " + bodyResponse.toString());
				os.write(bodyResponse.toString().getBytes("UTF-8"));
				os.flush();
				os.close();
				
				System.out.println("Code : " + con.getResponseCode() + "\nMessage : " + con.getResponseMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public String getBody(HttpExchange h) throws IOException {
			InputStreamReader isr = null;

			isr = new InputStreamReader(h.getRequestBody(), "utf-8");

			BufferedReader br = new BufferedReader(isr);
			// From now on, the right way of moving from bytes to utf-8
			// characters;
			int b;
			final StringBuilder buf = new StringBuilder();

			while ((b = br.read()) != -1) {
				buf.append((char) b);
			}

			br.close();
			isr.close();

			return buf.toString();
		}
	}

}
