package dao;

import model.Autor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AutorDAO {

    // Crear autor
    public int crear(Autor autor) throws SQLException {
        String sql = "INSERT INTO autor (nombre) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, autor.getNombre());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Leer todos los autores
    public List<Autor> obtenerTodos() throws SQLException {
        List<Autor> autores = new ArrayList<>();
        String sql = "SELECT id, nombre FROM autor ORDER BY nombre ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Autor autor = new Autor();
                autor.setId(rs.getInt("id"));
                autor.setNombre(rs.getString("nombre"));
                autores.add(autor);
            }
        }
        return autores;
    }

    // Leer autor por ID
    public Autor obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM autor WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Autor autor = new Autor();
                    autor.setId(rs.getInt("id"));
                    autor.setNombre(rs.getString("nombre"));
                    return autor;
                }
            }
        }
        return null;
    }

    // Actualizar autor
    public boolean actualizar(Autor autor) throws SQLException {
        String sql = "UPDATE autor SET nombre = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, autor.getNombre());
            pstmt.setInt(2, autor.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // Eliminar autor (cascada en BD eliminará sus avisos)
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM autor WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}