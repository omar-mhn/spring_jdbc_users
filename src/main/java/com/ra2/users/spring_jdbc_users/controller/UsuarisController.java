package com.ra2.users.spring_jdbc_users.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ra2.users.spring_jdbc_users.model.Usuari;
import com.ra2.users.spring_jdbc_users.repository.UsuariRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController

@RequestMapping("/api")
public class UsuarisController {
    @Autowired
    UsuariRepository usuariRepository;
    @PostMapping("users")
    public ResponseEntity<String> createUser(@RequestBody Usuari user) {
        usuariRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado correctamente");
    }
    
    @GetMapping("users")
    public ResponseEntity<List<Usuari>> getAllUsers() {
        List<Usuari> usuaris =  usuariRepository.findAll();
        if(usuaris == null || usuaris.isEmpty()){
            return ResponseEntity.ok(null);
        }else{
            return ResponseEntity.ok(usuaris);
        }
    }
    @GetMapping("users/{user_id}")
    public ResponseEntity<List<Usuari>> getUserById(@PathVariable Long user_id) {
       List<Usuari> usuaris =  usuariRepository.findUserById(user_id);
        if(usuaris == null || usuaris.isEmpty()){
            return ResponseEntity.ok(null);
        }else{
            return ResponseEntity.ok(usuaris);
        }
    }


    @PutMapping("users/{user_id}")
    public ResponseEntity<String> putUsuari(@PathVariable Long user_id, @RequestBody Usuari usuari) {
               
    int rowsUpdated = usuariRepository.update(user_id, usuari);

    if (rowsUpdated == 0) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuari amb id " +user_id + " no trobat");
    }

    return ResponseEntity.status(HttpStatus.OK)
            .body("Informació de l'usuari actualitzada correctament");
    }
    
    
    
    
}
