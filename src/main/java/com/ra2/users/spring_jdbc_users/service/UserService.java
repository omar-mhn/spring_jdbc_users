package com.ra2.users.spring_jdbc_users.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.sql.Timestamp;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ExceptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.ra2.users.spring_jdbc_users.logging.CustomLoggin;
import com.ra2.users.spring_jdbc_users.model.Usuari;
import com.ra2.users.spring_jdbc_users.repository.UsuariRepository;

@Service
public class UserService {

    @Autowired
    UsuariRepository usuariRepository;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private CustomLoggin customLoggin;

    public List<Usuari> findAll() {
        customLoggin.LogInfo("UserService", "findAll", "Find all users");
        try{
            return usuariRepository.findAll();    
        }catch(Exception e){
            customLoggin.LogError("UserService", "findAll", "Can't find all users in the database", e);
            throw e;
        }
    }
 public int addUser(Usuari user) {
    customLoggin.LogInfo("UserService", "addUser", "Creating a user");
    try {
        int numReg = usuariRepository.save(user);

        if (numReg > 0) {
            customLoggin.LogInfo("UserService", "addUser", "User created correctly");
        } else {
            customLoggin.LogError("UserService", "addUser", "Can't create the user", new Exception("No rows affected"));
        }
        return numReg;

    } catch (Exception e) {
        customLoggin.LogError("UserService", "addUser", "Can't create the user", e);
        throw e;
    }
}
    public List<Usuari> findUserById(long id) {
    customLoggin.LogInfo("UserService", "findUserById", "Find user with id " + id);
    
    try {
        List<Usuari> users = usuariRepository.findUserById(id);

        if (users.isEmpty()) {
            customLoggin.LogError("UserService", "findUserById", "User with id " + id + " not found", new Exception("User not found (empty list)"));
        } else {
             customLoggin.LogInfo("UserService", "findUserById", "User found");
        }

        return users;

    } catch (Exception e) {
        // Ce catch ne sert que si la BDD est cassée
        customLoggin.LogError("UserService", "findUserById", "Technical error searching user", e);
        throw e;
    }
}

    public int update(Long id, Usuari usuari) {
    customLoggin.LogInfo("UserService", "update", "Updating user with id " + id);
    try {
        int result = usuariRepository.update(id, usuari);

        if (result > 0) {
            customLoggin.LogInfo("UserService", "update", "User with id " + id + " has been updated");
        } else {
            customLoggin.LogError("UserService", "update", "User with id " + id + " doesn't exist", new Exception("User not found"));
        }
        return result;

    } catch (Exception e) {
        customLoggin.LogError("UserService", "update", "Technical error updating user " + id, e);
        throw e;
    }
}

   public int patch(long id, String newName) {
    customLoggin.LogInfo("UserService", "patch", "Update user with id " + id);
    try {
        int result = usuariRepository.patch(id, newName);

        if (result > 0) {
            customLoggin.LogInfo("UserService", "patch", "User with id " + id + " updated");
        } else {
            customLoggin.LogError("UserService", "patch", "User with id " + id + " doesn't exist", new Exception("User not found"));
        }
        return result;

    } catch (Exception e) {
        customLoggin.LogError("UserService", "patch", "Technical error patching user " + id, e);
        throw e;
    }
}

    public int delete(Long id){

        customLoggin.LogInfo("UserService", "delete", "Delete user with id "+id);
        try{
            int result = usuariRepository.delete(id);
            if(result>0){
                 customLoggin.LogInfo("UserService", "delete", "User with id "+id+" has been deleted");
            }else{
            customLoggin.LogError("UserService", "delete", "User with id "+id+" doesn't exist ", new Exception("User not fount"));

            }
           
            return result;

        }catch (Exception e){
            customLoggin.LogError("UserService", "delete", "Technical error during delete ", e);
            throw e;
        }
    }

    public String saveUserImage(long userId, MultipartFile imagFile) throws Exception {
        customLoggin.LogInfo("UserService", "saveUserImage", "Adding image " + imagFile.getOriginalFilename() + " to user with id " + userId);
        try {
            // 1. Correction logique : Vérifier si la liste est vide
            List<Usuari> user = usuariRepository.findUserById(userId);
            if (user.isEmpty()) {
                throw new Exception("User with id " + userId + " not found");
            }

            // 2. Créer dossier
            Path imagesFolder = Paths.get("src/main/resources/public/images");
            if (!Files.exists(imagesFolder)) {
                Files.createDirectories(imagesFolder);
            }

            // 3. Créer nom fichier
            String originalFileName = imagFile.getOriginalFilename();
            // Attention : gestion simple de l'extension, peut planter si pas de point
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = "User_" + userId + extension;
            Path filePath = imagesFolder.resolve(newFileName);

            // 4. Sauvegarder fichier
            try (InputStream inputStream = imagFile.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 5. Update BDD
            String imagePath = "/images/" + newFileName;
            usuariRepository.updateImagePath(userId, imagePath);
            
            // Correction espace manquant après le point
            customLoggin.LogInfo("UserService", "saveUserImage", "The image has been successfully saved. The path is: " + imagePath);

            return imagePath;

        } catch (Exception e) {
            // Correction espace manquant avant "doesn't"
            customLoggin.LogError("UserService", "saveUserImage", "User with id " + userId + " doesn't exist or error saving", e);
            throw e;
        }
    }

    public int saveUserCsv(MultipartFile csvFile) {
        customLoggin.LogInfo("UserService", "saveUserCsv", "Load information from this CSV file " + csvFile.getName());
        int comptador = 0;
        int errors = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String linia;
            int numLinia = 0;

            while ((linia = br.readLine()) != null) {
                numLinia++;
                if (numLinia == 1) continue; 
                if (linia.trim().isEmpty()) continue;
                
                String[] col = linia.split(",");
                if (col.length < 4) continue;

                String name = col[0];
                String description = col[1];
                String email = col[2];
                String password = col[3];

                Usuari user = new Usuari();
                user.setName(name);
                user.setDescription(description);
                user.setEmail(email);
                user.setPassword(password);
                user.setDataCreated(now);
                user.setDataUpdated(now);
                user.setUltimAcces(now);

                try {
                    usuariRepository.save(user);
                    comptador++;
                } catch (Exception e) {
                    customLoggin.LogError("UserService", "saveUserCsv", "Error on line " + linia, e);
                    errors++;
                }
            }

            Path csvFolder = Paths.get("src/main/resources/public/csv_processed");
            if (!Files.exists(csvFolder)) {
                try {
                    Files.createDirectories(csvFolder);
                } catch (Exception e) {
                    customLoggin.LogError("UserService", "saveUserCsv", "Cannot create directory", e);
                }
            }
            
            String originalFileName = csvFile.getOriginalFilename();
            Path filePath = csvFolder.resolve(originalFileName);
            try {
                Files.copy(csvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                customLoggin.LogError("UserService", "saveUserCsv", "Failed to save the file", e);
            }

        } catch (IOException e) {
            System.err.println("ERROR accessing file: " + e.getMessage());
            customLoggin.LogError("UserService", "saveUserCsv", "Error reading the file", e);
        }

        // Correction "records"
        customLoggin.LogInfo("UserService", "saveUserCsv", comptador + " records saved successfully, " + errors + " records failed.");
        return comptador;
    }

    public int saveUserJson(MultipartFile jsonFile) {
        // Correction "UserService" au lieu de "ServiceUser"
        customLoggin.LogInfo("UserService", "saveUserJson", "Loading information from file " + jsonFile.getName());
        int comptador = 0;
        int errors = 0; // Correction variable "erros"
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            JsonNode arrel = mapper.readTree(jsonFile.getInputStream());
            JsonNode data = arrel.path("data");
            
            int count = data.path("count").asInt();
            String control = data.path("control").asText();

            if (!control.equals("OK")) {
                return -1;
            }
            JsonNode users = data.path("users");

            if (users.size() != count) {
                return -3;
            }

            for (JsonNode user : users) {
                String name = user.path("name").asText();
                String description = user.path("description").asText();
                String email = user.path("email").asText();
                String password = user.path("password").asText();

                Usuari usuari = new Usuari(name, description, email, password, now, now, now);

                try {
                    usuariRepository.save(usuari);
                    comptador++;
                } catch (Exception e) {
                    customLoggin.LogError("UserService", "saveUserJson", "Error saving user", e);
                    errors++; // Correction variable
                    System.err.println("Error saving user"); // Traduction Anglais
                }
            }

        } catch (Exception e) {
            customLoggin.LogError("UserService", "saveUserJson", "Error reading Json", e);
            return -2;
        }

        try {
            Path directory = Paths.get("src/main/resources/public/json_processed/");
            Path targetFile = directory.resolve(jsonFile.getOriginalFilename());

            Files.createDirectories(directory);
            Files.copy(jsonFile.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            customLoggin.LogError("UserService", "saveUserJson", "Could not save the JSON file", e);
            System.err.println("Could not save the JSON file"); // Traduction Anglais
        }

        // Correction "records"
        customLoggin.LogInfo("UserService", "saveUserJson", comptador + " records saved successfully, " + errors + " records failed.");

        return comptador;
    }
}
    

