package main.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Handler extends Thread{
    private static final Map<String, String > CONTENT_TYPES = new HashMap<>(){{
        put("jpg","image/jpeg");
        put("html", "text/html");
        put("json", "application/json");
        put("txt", "text/plain");
        put("", "text/plain");
    }};
    private static final String NOT_FOUND_MESSAGE = "Not Fouund";
    private Socket socket;
    private String directory;

    public Handler(Socket socket, String dir){
        this.socket=socket;
        this.directory=dir;
    }



    @Override
    public void run(){
    try( var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()){
        var url= this.getRequestUrl(input);
    var filePath = Path.of(this.directory+url);
    if(Files.exists(filePath)&&!Files.isDirectory(filePath)){
        var extension = this.getFileExtension(filePath);
        var type =CONTENT_TYPES.get(extension);
        var fileBytes = Files.readAllBytes(filePath);
        this.sendHeader(output, 200, "OK", type, fileBytes.length);
        output.write(fileBytes);
    }
    else{
    var type = CONTENT_TYPES.get("text");
    this.sendHeader(output, 404, "Not Found", type,NOT_FOUND_MESSAGE.length());
    output.write(NOT_FOUND_MESSAGE.getBytes());
    }
        } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }
    private String getRequestUrl(InputStream input){
        var reader = new Scanner(input).useDelimiter("\r\n");
        String line =reader.next();
        System.out.println(line);
        return line.split(" ")[1];
    }

    private String getFileExtension(Path path){
    var name = path.getFileName().toString();
    var extensionStart = name.lastIndexOf(".");
    return extensionStart == -1 ? "":name.substring(extensionStart+1);
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long lenght) {
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("COntent-type: %s%n",type );
        ps.printf("Content-Length: %s%n%n", lenght);
    }
}