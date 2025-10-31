package com.ra2.users.spring_jdbc_users.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ra2.users.spring_jdbc_users.model.Usuari;

@Repository
public class UsuariRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final class UsuariRowMapper implements RowMapper<Usuari> {
    
        @Override
        public Usuari mapRow(ResultSet rs, int rowNum) throws SQLException {
            Usuari usuari = new Usuari();
            usuari.setId(rs.getLong("id"));
            usuari.setName(rs.getString("name"));
            usuari.setDescription(rs.getString("description"));
            usuari.setEmail(rs.getString("email"));
            usuari.setPassword(rs.getString("password"));
            usuari.setUltimAcces(rs.getTimestamp("ultimAcces"));
            usuari.setDataCreated(rs.getTimestamp("dataCreated"));
            usuari.setDataUpdated(rs.getTimestamp("dataUpdated"));
            return usuari;
        }
    }
    // CREATE - Inserta un usuario
    public int save(Usuari usuari){
        String sql = "INSERT INTO usuaris (name, description, email, password, ultimAcces, dataCreated, dataUpdated) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        return jdbcTemplate.update(sql, usuari.getName(), usuari.getDescription(), usuari.getEmail(), usuari.getPassword(), usuari.getUltimAcces());

    }

     // READ ALL - Obtiene todos los usuarios
    public List<Usuari> findAll() {
        String sql = "SELECT * FROM usuaris";
        return jdbcTemplate.query(sql, new UsuariRowMapper());
    }

    public List<Usuari> findUserById(long id) {
        String sql = "SELECT * FROM usuaris WHERE usuaris.id =?";
        return jdbcTemplate.query(sql, new UsuariRowMapper(),id);
    }
    
   public int update(Long id, Usuari usuari) {
    String sql = "UPDATE usuaris SET name = ?, description = ?, email = ?, password = ?, ultimAcces = ?, dataUpdated = NOW() WHERE id = ?";
    return jdbcTemplate.update(sql,
            usuari.getName(),
            usuari.getDescription(),
            usuari.getEmail(),
            usuari.getPassword(),
            usuari.getUltimAcces(),
            id);
    }

    public int patch(long id, String newName){
        String sql = "UPDATE usuaris SET name = ?, dataUpdated = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, newName, id);
    }


}
