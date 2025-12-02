package com.ra2.users.spring_jdbc_users.logging;


import org.springframework.stereotype.Component;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




@Component
public class CustomLoggin {

    public static final String LOG_FILE = "app.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");


}
