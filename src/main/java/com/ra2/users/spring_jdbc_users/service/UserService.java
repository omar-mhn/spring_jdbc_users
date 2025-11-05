package com.ra2.users.spring_jdbc_users.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
}
