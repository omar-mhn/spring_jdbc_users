package com.ra2.users.spring_jdbc_users.logging;


import org.springframework.stereotype.Component;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




@Component
public class CustomLoggin {

    public static final String LOG_FILE = "logs/app.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private void writeToFile(String msg){

        Path logPath = Paths.get(LOG_FILE);

        try{
            // devuelve eldirectorio parent, si no existe lo crea. 
            Files.createDirectories(logPath.getParent());

            try(BufferedWriter bw = Files.newBufferedWriter(logPath,
                 StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    bw.write(msg);// write message
                    bw.newLine();// add new line 

                 }
        }catch (IOException e) {
            System.err.println("ERROR escrivint al fitxer de log; "+ e.getMessage());
        }
    }

    public void LogError(String className, String method, String errorMsg,Exception exception){

        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[ERROR] %s - Class: %s - Method: %s - Description: %s ", timestamp,className,method,errorMsg);
        
        if(exception != null){
            logEntry += "Exception - " + exception.getMessage();
        }

        writeToFile(logEntry);
        System.out.println(logEntry);

    }  
    public void LogInfo(String classeName, String method, String infoMsg){
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[INFO] %s - Class: %s - Method : %s Description: %s",timestamp,classeName,method,infoMsg);

        writeToFile(logEntry);

        System.out.println(logEntry);
    }


    


}
