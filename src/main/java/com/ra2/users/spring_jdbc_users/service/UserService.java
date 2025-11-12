package com.ra2.users.spring_jdbc_users.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ra2.users.spring_jdbc_users.model.Usuari;
import com.ra2.users.spring_jdbc_users.repository.UsuariRepository;

@Service
public class UserService {

   @Autowired
    UsuariRepository usuariRepository;

    public List<Usuari> findAll() {
        return usuariRepository.findAll();
    }

    public int addUser(Usuari user){
        int numReg = usuariRepository.save(user);
        return numReg;
    }
    public List<Usuari> findUserById(long id){
        return usuariRepository.findUserById(id);
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

    
}
