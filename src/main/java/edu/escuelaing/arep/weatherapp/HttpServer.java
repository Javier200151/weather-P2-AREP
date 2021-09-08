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
    private static final String HTTP_MESSAGE = "HTTP/1.1 200 OK \r\n" 
                                                    + "Content-Type: text/html" + "\r\n"
                                                    + "\r\n";

    private static final String HTTP_MESSAGE_NOT_FOUND = "HTTP/1.1 404 Not Found\n"
                                                    + "Content-Type: text/html\r\n"
                                                    + "\r\n";
    private static final String WHEATER_QUERY = "https://api.openweathermap.org/data/2.5/weather?q=country&appid=d1bcfbab47d918a819df1b59af4eee93";

    private static final HttpServer _instance = new HttpServer();

    public static HttpServer getInstance(){
        return _instance;
    }

    private HttpServer(){
    }

    public String start() throws IOException{
        ServerSocket serverSocket = null;
        String jsonString="VACIOS";
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
                jsonString=serverConnection(clientSocket);
            } catch (URISyntaxException e) {
                System.err.println("URI incorrect.");
                System.exit(1);
            }
        }
        serverSocket.close();
        return jsonString;
    }

    public String serverConnection(Socket clientSocket) throws IOException, URISyntaxException {
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
        
		try {
			
			uriContentType=request.get(0).split(" ")[1];

            //URI resource = new URI(uriContentType);
            
            
        }catch(Exception e){
            System.out.println(e);
        }
        outputLine = getResource( uriContentType, outStream);
        //out.println(outputLine);
        out.close();
        in.close();
        clientSocket.close();

        return outputLine;
    }
    public String getResource( String uri, OutputStream outStream) throws URISyntaxException{
        
        if(uri.contains("clima")){
            //System.out.println(computeHTMLResponse(OutputStream out));
            return computeHTMLResponse(outStream);
        }else{
            String country = uri.substring(uri.lastIndexOf("=") + 1);
            System.out.println("country "+country);
            try {
                return computeJSONResponse(country);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }
    
    public static String computeJSONResponse(String country) throws IOException{
        String jsonText = HTTP_MESSAGE.replaceFirst("text", "application").replaceFirst("html", "json"); int cp;
        InputStream is = new URL(WHEATER_QUERY.replaceFirst("country", country)).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        while ((cp = rd.read()) != -1) jsonText += (char) cp;
        return jsonText;
    }

    public String computeHTMLResponse(OutputStream out){
        String content = HTTP_MESSAGE;
        File file = new File("src/main/resources/public/index.html");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line =  br.readLine()) != null) content += line + "\n"; 
            br.close();
            out.write(content.getBytes());
        } catch (IOException e) {
            System.err.format("FileNotFoundException %s%n", e);
            default404HTMLResponse(out);
        }
        return content;
    }
    private void default404HTMLResponse(OutputStream out){
        String outputline = HTTP_MESSAGE_NOT_FOUND;
        outputline +=     "<!DOCTYPE html>"
                        + "<html>"
                        +       "<head>"
                        +           "<title>404 Not Found</title>\n"
                        +           "<meta charset=\"UTF-8\">"
                        +           "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.8\">"
                        +           "<style type='text/css'>"
                        +               "h1{"
                        +                   "font-size: 150px;"
                        +                   "text-align: center;"
                        +           "</style>"
                        +       "</head>"
                        +       "<body>"
                        +           "<h1> Error 404 </h1>"
                        +       "</body>"
                        + "</html>";
        try {
            out.write(outputline.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

     
    
      public static JSONObject readJsonFromUrl(String city) throws IOException, JSONException {
        InputStream is = new URL(city).openStream();
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
