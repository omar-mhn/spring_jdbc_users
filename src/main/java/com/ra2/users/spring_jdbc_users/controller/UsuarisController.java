package com.ra2.users.spring_jdbc_users.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ra2.users.spring_jdbc_users.model.Usuari;
import com.ra2.users.spring_jdbc_users.service.UserService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController

@RequestMapping("/api")
public class UsuarisController {
    @Autowired
    UserService UserService;
    
    @PostMapping("users")
    public ResponseEntity<String> createUser(@RequestBody Usuari user) {
        UserService.addUser(user);
        return ResponseEntity.ok("Usuario creado correctamente");
    }
    @PostMapping("/users/{user_id}/image")
    public ResponseEntity<String> postImage(@PathVariable long user_id, @RequestParam ("imageFile") MultipartFile imageFile) {
        try{
            String imgUrl = UserService.saveUserImage(user_id, imageFile);
            return ResponseEntity.ok(imgUrl);
        }catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    @PostMapping("users/upload-csv")
    public ResponseEntity<String> postCsv(@RequestParam MultipartFile csvFile ) {
       if (csvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo CSV está vacío");
        }
        try {
            int totalReg = UserService.saveUserCsv(csvFile);
            return ResponseEntity.ok("Número de usuarios insertados: "+ totalReg);

        }catch (Exception e){
            return ResponseEntity.status(500).body("Error al procesar el archivo CSV: " + e.getMessage());
        }
    }
    @PostMapping("users/upload-json")
    public ResponseEntity<String> postJson(@RequestParam MultipartFile jsonFile) {
        if (jsonFile.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo CSV está vacío");
        }
        try {
            // Llamada al servicio que procesa el JSON y guarda los usuarios.
            int result = UserService.saveUserJson(jsonFile);

            if (result == -1){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Control incorrecto en el JSON (control != \"OK\").");
            } else if(result == -2){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error leyendo o parseando el JSON.");
            }else if(result == -3){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El campo count no coincide con el número de usuarios en el JSON.");
            }else{
                return ResponseEntity.status(HttpStatus.OK).body("Registros añadidos: " + result);
            }

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al procesar el archivo.");
        }
        
        
    }
    
    
    @GetMapping("users")
    public ResponseEntity<List<Usuari>> getAllUsers() {
        List<Usuari> usuaris =  UserService.findAll();
        if(usuaris == null || usuaris.isEmpty()){
            return ResponseEntity.ok(null);
        }else{
            return ResponseEntity.ok(usuaris);
        }
    }
    @GetMapping("users/{user_id}")
    public ResponseEntity<List<Usuari>> getUserById(@PathVariable Long user_id) {
       List<Usuari> usuaris =  UserService.findUserById(user_id);
        if(usuaris == null || usuaris.isEmpty()){
            return ResponseEntity.ok(null);
        }else{
            return ResponseEntity.ok(usuaris);
        }
    }


    @PutMapping("users/{user_id}")
    public ResponseEntity<String> putUsuari(@PathVariable Long user_id, @RequestBody Usuari usuari) {
               
        int rowsUpdated = UserService.update(user_id, usuari);

        if (rowsUpdated == 0) {
            return ResponseEntity.ok("Usuari amb id " +user_id + " no trobat");
        }

        return ResponseEntity.ok("Informació de l'usuari actualitzada correctament");
        }

    @PatchMapping("users/{user_id}/name")
    public ResponseEntity<List<Usuari>> patchUser(@PathVariable long user_id,@RequestParam String name) {
        int rows = UserService.patch(user_id, name);
        if(rows == 0){
            return ResponseEntity.ok(null);
        }
        List<Usuari> updatedUser =UserService.findUserById(user_id);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("users/{user_id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long user_id){
        int rows = UserService.delete(user_id);

        if (rows == 0){
            return ResponseEntity.ok("Usuari amb id " + user_id + " no trobat");
        }
            return ResponseEntity.ok("Usuari eliminat correctament");

    }

   
    
 
    
        
   
    
    
}
