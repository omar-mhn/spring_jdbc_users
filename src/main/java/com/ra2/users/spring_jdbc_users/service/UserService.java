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
    public int addUser(Usuari user){
        customLoggin.LogInfo("UserService", "addUser", "Add a user in the database");
        try{
            int numReg = usuariRepository.save(user);
            return numReg;
        }catch(Exception e ){
            customLoggin.LogError("UserService", "addUser","Can't add user" , e);
            throw e ;
        }
        
    }
    public List<Usuari> findUserById(long id){
        customLoggin.LogInfo("UserService", "findUserById", "Find a user with id" + id);
        try{
            return usuariRepository.findUserById(id);
        }catch (Exception e){
            customLoggin.LogError("UserSerive","findUserById", "User with id "+id+" not found", e);
            throw e ;
        }
        
    }

    public int update(Long id, Usuari usuari){
        return usuariRepository.update(id, usuari);
    }

    public int patch(long id, String newName){
        return usuariRepository.patch(id, newName);
    }

    public int delete(Long id){
        return usuariRepository.delete(id);
    }

    public String saveUserImage(long userId, MultipartFile imagFile) throws Exception{
        // Verificar el usuario 
        List <Usuari> user = usuariRepository.findUserById(userId);
        System.out.println(user);
        if(user.isEmpty()){
            throw new Exception("Usuari amb id " + userId + " no trobat");
        }

        // Crear carpeta si necesario 
        Path imagesFolder = Paths.get("src/main/resources/public/images");
        if(!Files.exists(imagesFolder)){
            Files.createDirectories(imagesFolder);
        }

        // Crear el nom delfitxer

        String originalFileName = imagFile.getOriginalFilename();
        String extention = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = "User_" + userId+extention;
        // resolve permet de combiner le dossier et le nom du fichier pour obtenir le chemin complet
        Path filePath = imagesFolder.resolve(newFileName);

        // Desar el fitxer 
        //imageFile.getInputStream() permite leer el contenido del archivo que envía el client
        try(InputStream inputStream = imagFile.getInputStream()){
            //StandardCopyOption.REPLACE_EXISTING sobrescribe un archivo con el mismo nombre si ya existe.
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        //Actualizar la ruta en la base de datos
        String imagePath = "/images/" + newFileName;
        usuariRepository.updateImagePath(userId, imagePath);

        //URL de l?image
        return imagePath;
    }
    public int saveUserCsv(MultipartFile csvFile){
        
        int comptador = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis()); 
        try(BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))){

            String linia; 
            int numLinia = 0;

                while ((linia = br.readLine()) != null) {
                    numLinia ++;
                    if(numLinia == 1){
                        continue; // Saltar capçalera
                    }
                    if (linia.trim().isEmpty()) {
                    continue;
                    }
                    String [] col = linia.split(",");
                    if (col.length < 4) continue;

                    String name = col[0];
                    String description = col[1];
                    String email = col[2];
                    String password = col[3];

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    Usuari user = new Usuari();
                    user.setName(name);
                    user.setDescription(description);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setDataCreated(timestamp);
                    user.setDataUpdated(timestamp);
                    user.setUltimAcces(timestamp);

                    usuariRepository.save(user);
                    comptador++;           
                }
            
                Path csvFolder = Paths.get("src/main/resources/public/csv_processed");
                    if(!Files.exists(csvFolder)){
                        Files.createDirectories(csvFolder);
                    }
                    String originalFileName = csvFile.getOriginalFilename();
                    Path filePath = csvFolder.resolve(originalFileName);
                    Files.copy(csvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        } catch (IOException e) {
            // Errors generals d'E/S
            System.err.println("ERROR d'accés al fitxer: " + e.getMessage());
        }
            return comptador;
    }


    public int saveUserJson(MultipartFile jsonFile){
        int comptador = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        // Leer el contenido del archivo JSON
        try{
            JsonNode arrel = mapper.readTree(jsonFile.getInputStream());
            JsonNode data = arrel.path("data");
            // Obtener el valor de count y control
            int count = data.path("count").asInt();
            String control = data.path("control").asText();
            //Comprobar que el control sea "OK"
            if (!control.equals("OK")){
                return -1 ; // Control incorrecto
            }
            JsonNode users = data.path("users");
            //Comprobar que la lista de usuarios tenga el mismo tamaño que count 

            if(users.size() != count ){
                return -3; // El número de usuarios no coincide
            }

            for(JsonNode user:users){
                String name = user.path("name").asText();
                String description = user.path("description").asText();
                String email = user.path("email").asText();
                String password = user.path("password").asText();

                Usuari usuari = new Usuari(name, description, email, password, now, now, now);
                
            // Intentar guardar en la base de datos
                try{
                    usuariRepository.save(usuari);
                    comptador ++;
                } catch (Exception e) {
                System.err.println("Error guardando el usuario");
                }
            }
            
        // Error leyendo el JSON
        }catch(Exception e){
            return -2;
        }
        //Guardar el archivo JSON procesado en la carpeta json_processed
        try{
         // Crear la carpeta si no existe
            Path directory = Paths.get("src/main/resources/public/json_processed/");
            Path targetFile = directory.resolve(jsonFile.getOriginalFilename());

            Files.createDirectories(directory);

            // Guardar el archivo, reemplazando si ya existe
            Files.copy(jsonFile.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        
        }catch (Exception e){
            System.err.println("No se ha podido guardar el JSON");
        }
          
        return comptador;
        
    }

}
    

