package fr.univlille1.m2iagl.kruczek.petit;

import fr.univlille1.m2iagl.kruczek.petit.Fichier;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
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
	public String repoUrl;
	
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
			ArrayList fichiers;
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
				String headContentUrl = obj.getJSONObject("pull_request")
						.getJSONObject("head")
						.getJSONObject("repo")
						.getString("contents_url") ;
				
				int numberPull = obj.getInt("number");
				System.out.println("\tPull URL : " + pullUrl);
				System.out.println("\tDiff URL : " + diffUrlStr);
				System.out.println("\tState : " + pullState);

				if (!pullState.equals("closed")) {
					
					final Date date = new java.util.Date();
					final String fileName = numberPull + "_" + date.getTime();

					getCheckstyleFile(fileName,
							pullUrl.substring(0, pullUrl.indexOf("/pulls/")));
					fichiers= getJavaDiffFile(fileName, diffUrlStr);
					
					for(int i =0;i<fichiers.size();i++){
						Fichier f= (Fichier) fichiers.get(i);
						dlFile(headContentUrl,f.path,fileName);
					}
					executeCheckstyle(fileName);
					commentPR(issueUrl, Start.token, fileName);
					
				} else {
					System.out.println("The pull request is closed, no need to read");
				}
			}
		}
		
		/**
		 * Execution de la commande Checkstyle
		 * @param fileName
		 * @throws IOException
		 */
		public void executeCheckstyle(String fileName) throws IOException {
			String checkstyle="lib/checkstyle-7.1.2-all.jar -c";
			String checkstyleFile="tmp/checkstyle/"+fileName+".xml" ;
			String javaFile="tmp/in/"+fileName+".java";
			String outputFile = "tmp/out/"+fileName+".check";
			String inputFold="tmp/in/"+fileName+"/";
			String command="java -jar "+checkstyle+" "+checkstyleFile+" "+inputFold+" -o "+outputFile;
			System.out.println(command);
			
			//creation du dossier out
			File file = new File(outputFile);
			file.getParentFile().mkdirs();
			
			//execution de la command
			Process cat = Runtime.getRuntime().exec(command);
			BufferedReader stdOut=new BufferedReader(new InputStreamReader(cat.getInputStream()));
	        String s;
	        while((s=stdOut.readLine())!=null){
	            //nothing or print
	        }
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
				//xmlStr = xmlStr.replace("<module name=\"Checker\">","<module name=\"Checker\">\n"+supComFilter);
				//xmlStr = xmlStr.replace("<module name=\"TreeWalker\">","<module name=\"TreeWalker\">\n"+fileConHolder);
				
				fos.write(xmlStr.getBytes());
				fos.flush();
				fos.close();

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		
		public ArrayList getJavaDiffFile(String fileName, String diffUrlStr) throws IOException{
			final URL diffURL = new URL(diffUrlStr);
			final URLConnection diffUrlCon = diffURL.openConnection();
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(diffUrlCon.getInputStream()));
			
			final String inFileName = "tmp/in/" + fileName + ".java";
		
			//File inFile = new File(inFileName);
			//inFile.getParentFile().mkdirs();
			//inFile.createNewFile();
			//final FileOutputStream fos = new FileOutputStream(inFile);
			
			String inputLine;

			boolean javaDiff = false;
			boolean javaLine=false;
			//lecture des differences
			//fos.write("//CHECKSTYLE.OFF: UnusedImports\n".getBytes());
			
			ArrayList fichiers = new ArrayList();
			Fichier currentFichier=null;
			String currentLine="";
			String nM;
			String nP;
			int iLigne=0;
			while ((inputLine = in.readLine()) != null) {
				
				if (inputLine.startsWith("diff --git") && inputLine.endsWith(".java")) {//commande diff sur les fichiers java
					javaLine=false;
					javaDiff = true;
					//System.out.println("diff sur fichiers java : "+inputLine);
					//System.out.println(inputLine.split(" ")[2].substring(1));
					if(currentFichier != null){
						
						currentFichier.lignes.add(currentLine);
						currentLine="";
						fichiers.add(currentFichier);
						currentFichier=null;
					}
					currentFichier=new Fichier(inputLine.split(" ")[2].substring(1));
					
					
				} else if (inputLine.startsWith("diff --git") && !inputLine.endsWith(".java")) { // commande diff sur les autres fichiers
					javaLine=false;
					if(currentFichier != null){
						currentFichier.lignes.add(currentLine);
						fichiers.add(currentFichier);
						currentFichier=null;
					}
					javaDiff = false;
					//System.out.println();
				}
					
				if (javaDiff == true) { //lignes en java
					
					if(inputLine.startsWith("@@")){
						//recuperation du numero de ligne
						if(currentLine.length()!=0){
							currentFichier.lignes.add(currentLine);
						}
						//System.out.println("ligne : "+inputLine);
						//System.out.println("ligne : "+inputLine.split("-")[1].split(",")[0]);
						//System.out.println("ligne : "+inputLine.split("\\+")[1].split(",")[0]);
						nM=inputLine.split("-")[1].split(",")[0];
						nP=inputLine.split("\\+")[1].split(",")[0];
						if( Integer.parseInt(nM) <= Integer.parseInt(nP) ){
							//currentLine=nM+"\n" ;
							//currentFichier.iLignes.add(nM);
							iLigne=Integer.parseInt(nM);
						}else{
							//currentLine=nP+"\n" ;
							//currentFichier.iLignes.add(nP);
							iLigne=Integer.parseInt(nP);
						}
						javaLine=true;
						
					}else if(javaLine==true){ //recuperation des lignes
						
						currentLine=currentLine+inputLine+"\n";
						if(inputLine.startsWith("+")){
							currentFichier.iLignes.add(""+iLigne);
						}
						iLigne++;
					}
					
					/*if (inputLine.startsWith("+")
							&& !inputLine.startsWith("+++")) {
						//System.out.println(inputLine);
						fos.write((inputLine.substring(1).trim() + "\n")
								.getBytes());
					}*/
					
				}
			}
			if(currentFichier != null){
				currentFichier.lignes.add(currentLine);
				fichiers.add(currentFichier);
			}
			//fos.write("//CHECKSTYLE.ON: UnusedImports".getBytes());
			in.close();
			//fos.flush();
			//fos.close();
			for(int i = 0;i<fichiers.size();i++){
				Fichier f = (Fichier) fichiers.get(i);
				//System.out.println(f.path);
				//System.out.println(f.iLignes.get(0));
				for(int j=0;j<f.iLignes.size();j++){
					//System.out.println("ligne : "+j);
					//System.out.println(f.iLignes.get(j));
				}
			}
			return fichiers;
		}
		
		public void dlFile(String contentPath, String path, String folderName) throws IOException{
			System.out.println("dl de :"+contentPath.replace("/{+path}",path));
			final URL url = new URL(contentPath.replace("/{+path}",path));
			
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
			String[] nom=path.split("/");
			//System.out.println("tmp/in/"+folderName+"/"+nom[nom.length-1]);
			File file = new File("tmp/in/"+folderName+"/"+nom[nom.length-1]);
			file.getParentFile().mkdirs();
			file.createNewFile();
			final FileOutputStream fos = new FileOutputStream(file);
			
			//decodage
			byte[] b64Decoded = Base64.decodeBase64( obj.getString("content").getBytes());
			
			//ajout des checkstyle on/off
			String xmlStr = new String(b64Decoded, StandardCharsets.UTF_8);
			//String[] lines = xmlStr.split("\n");
			//System.out.println("ok:"+lines.length);
			//xmlStr="";
			
			int iFichier=0;
			//System.out.println( ((String) linesToAdd.get(iFichier)));
			
			
			//System.out.println(xmlStr);
			//xmlStr = xmlStr.replace("<module name=\"Checker\">","<module name=\"Checker\">\n"+supComFilter);
			//xmlStr = xmlStr.replace("<module name=\"TreeWalker\">","<module name=\"TreeWalker\">\n"+fileConHolder);
			
			fos.write(xmlStr.getBytes());
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
				
				String commentIntro = "Generate Comment by CheckStyle :\n";
				String commentText="";
				//BufferedReader reader = new BufferedReader(new FileReader("tmp/out/"+outCheckFile+".check"));
				
				
				
				String filePath = new File("").getAbsolutePath();
				//System.out.println(filePath + "/tmp/out/"+outCheckFile+".check");
				BufferedReader reader = new BufferedReader(new FileReader(filePath + "/tmp/out/"+outCheckFile+".check"));                    
			    String line = null;   
			    int nbl=0;
			    while ((line = reader.readLine()) != null)
			    {
			        if (!(line.startsWith("*")))
			        {
			        	nbl++;
			            commentText=commentText+line+"\n";
			        }
			    }               
			    reader.close();
			    if(nbl<=2){
			    	commentText="No problem with checkstyle";
			    }
				
				bodyResponse.put("body",commentIntro+commentText);
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
