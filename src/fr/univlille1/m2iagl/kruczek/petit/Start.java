package fr.univlille1.m2iagl.kruczek.petit;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Start {

	public static String token = "";// Antoine token =
									// db948b18b3c47a3ac8b396f224c299a018b4b2b0

	public static void main(String[] args) throws IOException {
		System.out
				.println("Welcome in initiation of the check pull request server : ");
		System.out
				.println("Please enter the token needed to write a comment : ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		token = br.readLine();
		System.out
				.println("Your token is taken in account, waiting for webhook");

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

			final JSONObject obj = new JSONObject(bodyString);
			if (obj.has("pull_request")) {

				System.out.println("Receiving a new pull request event : ");
				final String pullUrl = obj.getJSONObject("pull_request")
						.getString("url");
				final String diffUrl = obj.getJSONObject("pull_request")
						.getString("diff_url");
				final String issueUrl = obj.getJSONObject("pull_request")
						.getString("issue_url");
				final String pullState = obj.getJSONObject("pull_request")
						.getString("state");
				int numberPull = obj.getInt("number");
				System.out.println("\tPull URL : " + pullUrl);
				System.out.println("\tDiff URL : " + diffUrl);
				System.out.println("\tState : " + pullState);

				if (!pullState.equals("closed")) {
					final URL yahoo = new URL(diffUrl);
					final URLConnection yc = yahoo.openConnection();
					final BufferedReader in = new BufferedReader(
							new InputStreamReader(yc.getInputStream()));
					final Date date = new java.util.Date();

					final String fileName = numberPull + "_" + date.getTime();

					getCheckstyleFile(fileName,
							pullUrl.substring(0, pullUrl.indexOf("/pulls/")));

					final String inFileName = "./tmp/in/" + fileName + ".java";
//					final String outFileName = "./tmp/out/" + fileName
//							+ ".check";
					final FileOutputStream fos = new FileOutputStream(
							inFileName);
					String inputLine;

					boolean javaLine = false;
					while ((inputLine = in.readLine()) != null) {

						if (inputLine.startsWith("diff --git")
								&& inputLine.endsWith(".java")) {
							javaLine = true;
							System.out.println(inputLine);
						} else if (inputLine.startsWith("diff --git")
								&& !inputLine.endsWith(".java")) {
							javaLine = false;
							System.out.println();
						}

						if (javaLine == true) {
							if (inputLine.startsWith("+")
									&& !inputLine.startsWith("+++")) {
								System.out.println(inputLine);
								fos.write((inputLine.substring(1) + "\n")
										.getBytes());
							}
						}
					}
					in.close();
					fos.flush();
					fos.close();

					String response = "This is the response";

					httpExchange.sendResponseHeaders(202, response.length());
					final OutputStream os = httpExchange.getResponseBody();
					os.write(response.getBytes());
					os.close();
					checkFile(fileName);
//					commentPR(issueUrl, Start.token);
				} else {
					System.out
							.println("The pull request is closed, no need to read");
				}
			}
		}
		
		public static void checkFile (final String fileName) {
			ProcessBuilder pb = new ProcessBuilder("java","-jar", "./lib/checkstyle-7.1.2-all.jar","-c", "./tmp/checkstyle/"+fileName+".xml", "-o", "./tmp/out/"+fileName+".out","./tmp/in/"+fileName+".java");
			System.out.println("Starting the process");
			try {
				Process p = pb.start();
				System.out.println("Exit Value : " +p.exitValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static void getCheckstyleFile(final String fileName,
				final String repoUrl) {
			final String checkstyleFileName = "./tmp/checkstyle/" + fileName
					+ ".xml";
			final String stringUrl = repoUrl + "/contents/checkstyle.xml";
			System.out.println("Url pour contents : " + stringUrl);
			try {
				final URL url = new URL(stringUrl);
				final HttpsURLConnection con = (HttpsURLConnection) url
						.openConnection();

				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Authorization", "token " + token);
				con.setRequestProperty("Accept", "application/vnd.github-blob.raw");
				con.setDoInput(true);

				final BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				final StringBuilder sb = new StringBuilder();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
				
				final FileOutputStream fos = new FileOutputStream(
						checkstyleFileName);
				fos.write(sb.toString().getBytes());
				fos.flush();
				fos.close();

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public static void commentPR(final String issueUrl, final String token) {
			System.out.println("Writing the comment");
			final String stringUrl = issueUrl + "/comments";
			System.out.println("Write to url : " + stringUrl);

			try {
				final URL url = new URL(stringUrl);
				final HttpsURLConnection con = (HttpsURLConnection) url
						.openConnection();

				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Authorization", "token " + token);
				con.setRequestProperty("content-type",
						"application/json; charset=UTF-8");
				con.setDoOutput(true);

				final JSONObject bodyResponse = new JSONObject();
				bodyResponse.put("body",
						"OK : This Pull Request has a good code style");
				final OutputStream os = con.getOutputStream();
				System.out.println("Sending message : "
						+ bodyResponse.toString());
				os.write(bodyResponse.toString().getBytes("UTF-8"));
				os.flush();
				os.close();

				System.out.println("Code : " + con.getResponseCode()
						+ "\nMessage : " + con.getResponseMessage());
			} catch (final Exception e) {
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
