package edu.escuelaing.arep.weatherapp;

import java.net.*;
import java.nio.charset.Charset;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpServer {
    private static final HttpServer _instance = new HttpServer();
    private static final HashMap<String,String> contentType = new HashMap<String,String>();
    public static HttpServer getInstance(){
        contentType.put("html","text/html");
        contentType.put("css","text/css");
		contentType.put("js","text/javascript");
        return _instance;
    }

    private HttpServer(){
        
    }
    

    public void start() throws IOException{
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(getPort());
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            try {
                serverConnection(clientSocket);
            } catch (URISyntaxException e) {
                System.err.println("URI incorrect.");
                System.exit(1);
            }
        }
        serverSocket.close();
    }

    public void serverConnection(Socket clientSocket) throws IOException, URISyntaxException {
        OutputStream outStream=clientSocket.getOutputStream();
		PrintWriter out = new PrintWriter(outStream, true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream()));
                      
        String inputLine, outputLine;
        ArrayList<String> request = new ArrayList<>();
        String sv="";
        

        while ((inputLine = in.readLine()) != null) {
            request.add(inputLine);
            if (!in.ready()) {
                break;
            }
        }
        System.out.println("request "+request.get(0).split(" ")[1]);
        String uriContentType="";
		String uri="";
		try {
			
			uriContentType=request.get(0).split(" ")[1];

            URI resource = new URI(uriContentType);
            String country = uriContentType.substring(uriContentType.lastIndexOf("=") + 1);
            System.out.println("country "+country);
            JSONObject json = readJsonFromUrl("api.openweathermap.org/data/2.5/weather?q="+country+"&appid=d1bcfbab47d918a819df1b59af4eee93");
            System.out.println(json.toString());
            //System.out.println(json.get("id"));
			
			uri=resource.getPath().split("/")[1];
        }catch(Exception e){
            System.out.println(e);
        }
        //outputLine = getResource( uri, outStream);
        //out.println(outputLine);
        out.close();
        in.close();
        clientSocket.close();
    }
    public String getResource( String uri, OutputStream outStream) throws URISyntaxException{
        return computeContentResponse(uri);
    }

    public String computeContentResponse(String uriContentType){
        String extensionUri = uriContentType.substring(uriContentType.lastIndexOf(".") + 1);
        String content = "HTTP/1.1 200 OK \r\n" 
                            + "Content-Type: "+ contentType.get(extensionUri) + "\r\n"
                            + "\r\n";
        File file = new File("src/main/resources/public/"+uriContentType);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line =  br.readLine()) != null) content += line; 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public String computeDefaultResponse(){
        String outputLine =
                "HTTP/1.1 200 OK\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Title of the document</title>\n"
                        + "</head>"
                        + "<body>"
                        + "My Web Site"
                        + "</body>"
                        + "</html>";
        return outputLine;
    }
    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35000; //returns default port if heroku-port isn't set (i.e. on localhost)
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }
    
      public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json;
        } finally {
          is.close();
        }
      }
    public static void main(String[] args) throws IOException {
        HttpServer.getInstance().start();
    }
}
