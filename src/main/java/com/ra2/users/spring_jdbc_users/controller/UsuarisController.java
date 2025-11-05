package com.ra2.users.spring_jdbc_users.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ra2.users.spring_jdbc_users.model.Usuari;
import com.ra2.users.spring_jdbc_users.service.UserService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

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
