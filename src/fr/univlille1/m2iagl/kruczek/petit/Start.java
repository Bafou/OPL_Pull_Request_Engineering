package fr.univlille1.m2iagl.kruczek.petit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Start {

	public static String token = "";// Antoine token =
									// db948b18b3c47a3ac8b396f224c299a018b4b2b0
	// Remi token
	//public static String token = "4bc78b59f3afd601aa44445fc18f92592c19af3d";
	
	
	public static void main(String[] args) throws IOException {
		System.out
				.println("Welcome in initiation of the check pull request server : ");
		
		token= args[0];
		if(token.equals("")){
			System.out
					.println("Please enter the token needed to write a comment : ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			token = br.readLine();
		}
		
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
			//recuperation du body de la requete
			String bodyString = null;
			try {
				bodyString = getBody(httpExchange);
			} catch (final IOException e) {
				System.out.println("impossible to get body request");
				e.printStackTrace();
			}
			
			//envoie de la reponse
			String response = "This is the response";
			httpExchange.sendResponseHeaders(202, response.length());
			final OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();

			
			final JSONObject obj = new JSONObject(bodyString);
			if (obj.has("pull_request")) {

				System.out.println("Receiving a new pull request event : ");
				final String pullUrl = obj.getJSONObject("pull_request")
						.getString("url");
				final String diffUrlStr = obj.getJSONObject("pull_request")
						.getString("diff_url");
				final String issueUrl = obj.getJSONObject("pull_request")
						.getString("issue_url");
				final String pullState = obj.getJSONObject("pull_request")
						.getString("state");
				int numberPull = obj.getInt("number");
				System.out.println("\tPull URL : " + pullUrl);
				System.out.println("\tDiff URL : " + diffUrlStr);
				System.out.println("\tState : " + pullState);

				if (!pullState.equals("closed")) {
					
					final Date date = new java.util.Date();
					final String fileName = numberPull + "_" + date.getTime();

					getCheckstyleFile(fileName,
							pullUrl.substring(0, pullUrl.indexOf("/pulls/")));
					getJavaDiffFile(fileName, diffUrlStr);
					executeCheckstyle(fileName);
					//commentPR(issueUrl, Start.token, fileName);
					
				} else {
					System.out.println("The pull request is closed, no need to read");
				}
			}
		}
		
		public void executeCheckstyle(String fileName) throws IOException {
			String checkstyle="lib/checkstyle-7.1.2-all.jar -c";
			String checkstyleFile="tmp/checkstyle/"+fileName+".xml" ;
			String javaFile="tmp/in/"+fileName+".java";
			String outputFile = "tmp/out/"+fileName+".check";
			String command="java -jar "+checkstyle+" "+checkstyleFile+" "+javaFile+" -o "+outputFile;
			System.out.println(command);
			
			//creation du dossier out
			File file = new File(outputFile);
			file.getParentFile().mkdirs();
			
			//execution de la command
			Process cat = Runtime.getRuntime().exec(command);
		}

		public static void getCheckstyleFile(final String fileName,final String repoUrl) {
			String supComFilter = "<module name=\"SuppressionCommentFilter\">\n"+
					"<property name=\"offCommentFormat\" value=\"CHECKSTYLE.OFF\\: ([\\w\\|]+)\"/>\n"+
					"<property name=\"onCommentFormat\" value=\"CHECKSTYLE.ON\\: ([\\w\\|]+)\"/>\n"+
					"<property name=\"checkFormat\" value=\"$1\"/>\n"+"</module>\n";
			String fileConHolder = "<module name=\"FileContentsHolder\"/>\n";
			
			final String checkstyleFileName = "tmp/checkstyle/" + fileName+ ".xml";
			final String stringUrl = repoUrl + "/contents/checkstyle.xml";
			
			System.out.println("Url pour contents : " + stringUrl);
			try {
				final URL url = new URL(stringUrl);
				final HttpsURLConnection con = (HttpsURLConnection) url
						.openConnection();

				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Authorization", "token " + token);
				con.setRequestProperty("content-type",
						"application/json; charset=UTF-8");
				con.setDoInput(true);

				final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				final StringBuilder sb = new StringBuilder();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);	
				}
				final JSONObject obj = new JSONObject(sb.toString());
				
				//creation du fichier checkstyle
				File file = new File(checkstyleFileName);
				file.getParentFile().mkdirs();
				file.createNewFile();
				final FileOutputStream fos = new FileOutputStream(file);
				
				//decodage
				byte[] b64Decoded = Base64.decodeBase64( obj.getString("content").getBytes());
				
				//ajout des checkstyle on/off
				String xmlStr = new String(b64Decoded, StandardCharsets.UTF_8);
				xmlStr = xmlStr.replace("<module name=\"Checker\">","<module name=\"Checker\">\n"+supComFilter);
				xmlStr = xmlStr.replace("<module name=\"TreeWalker\">","<module name=\"TreeWalker\">\n"+fileConHolder);
				
				fos.write(xmlStr.getBytes());
				fos.flush();
				fos.close();

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		
		public void getJavaDiffFile(String fileName, String diffUrlStr) throws IOException{
			final URL diffURL = new URL(diffUrlStr);
			final URLConnection diffUrlCon = diffURL.openConnection();
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(diffUrlCon.getInputStream()));
			
			final String inFileName = "tmp/in/" + fileName + ".java";
		
			File inFile = new File(inFileName);
			inFile.getParentFile().mkdirs();
			inFile.createNewFile();
			final FileOutputStream fos = new FileOutputStream(inFile);
			
			String inputLine;

			boolean javaLine = false;
			//lecture des differences
			fos.write("//CHECKSTYLE.OFF: UnusedImports\n".getBytes());

			while ((inputLine = in.readLine()) != null) {
				
				if (inputLine.startsWith("diff --git")
						&& inputLine.endsWith(".java")) {//commande diff sur les fichiers java
					javaLine = true;
					//System.out.println(inputLine);		
				} else if (inputLine.startsWith("diff --git")
						&& !inputLine.endsWith(".java")) { // commande diff sur les autres fichiers
					javaLine = false;
					//System.out.println();
				}
				if (javaLine == true) { //lignes en java
					if (inputLine.startsWith("+")
							&& !inputLine.startsWith("+++")) {
						//System.out.println(inputLine);
						fos.write((inputLine.substring(1) + "\n")
								.getBytes());
					}
				}
			}
			fos.write("//CHECKSTYLE.ON: UnusedImports".getBytes());
			in.close();
			fos.flush();
			fos.close();
		}

		public static void commentPR(final String issueUrl, final String token, String outCheckFile) {
			System.out.println("Writing the comment");
			final String stringUrl = issueUrl + "/comments";
			System.out.println("Write to url : " + stringUrl);

			try {
				final URL url = new URL(stringUrl);
				final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Authorization", "token " + token);
				con.setRequestProperty("content-type","application/json; charset=UTF-8");
				con.setDoOutput(true);

				final JSONObject bodyResponse = new JSONObject();
				
				String commentText = "Generate Comment by CheckStyle :";
				
				bodyResponse.put("body","OK : This Pull Request has a good code style");
				final OutputStream os = con.getOutputStream();
				System.out.println("Sending message : "+ bodyResponse.toString());
				os.write(bodyResponse.toString().getBytes("UTF-8"));
				os.flush();
				os.close();

				System.out.println("Code : " + con.getResponseCode()+ "\nMessage : " + con.getResponseMessage());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public String getBody(HttpExchange h) throws IOException {
			InputStreamReader isr = null;

			isr = new InputStreamReader(h.getRequestBody(), "utf-8");

			BufferedReader br = new BufferedReader(isr);
			
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
