package DAO;

import model.Autor;
import model.Aviso;
import model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisoDAO {

    // Crear aviso
    public int crear(Aviso aviso) throws SQLException {
        String sql = "INSERT INTO aviso (titulo, descripcion, estado, autor_id, categoria_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, aviso.getTitulo());
            pstmt.setString(2, aviso.getDescripcion());
            pstmt.setString(3, aviso.getEstado());
            pstmt.setInt(4, aviso.getAutor().getId());
            pstmt.setInt(5, aviso.getCategoria().getId());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Leer todos los avisos (con JOIN para autor y categoría)
    public List<Aviso> obtenerTodos() throws SQLException {
        List<Aviso> avisos = new ArrayList<>();
        String sql = "SELECT a.id, a.titulo, a.descripcion, a.fecha_creacion, a.estado, " +
                "au.id AS autor_id, au.nombre AS autor_nombre, " +
                "c.id AS categoria_id, c.nombre AS categoria_nombre " +
                "FROM aviso a " +
                "INNER JOIN autor au ON a.autor_id = au.id " +
                "INNER JOIN categoria c ON a.categoria_id = c.id " +
                "ORDER BY a.fecha_creacion DESC";

        try (Connection conn = ConexionBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Aviso aviso = new Aviso();
                aviso.setId(rs.getInt("id"));
                aviso.setTitulo(rs.getString("titulo"));
                aviso.setDescripcion(rs.getString("descripcion"));
                aviso.setEstado(rs.getString("estado"));

                // Parsear fecha con formato MySQL
                Timestamp timestamp = rs.getTimestamp("fecha_creacion");
                if (timestamp != null) {
                    aviso.setFechaCreacion(timestamp.toLocalDateTime());
                }

                // Autor
                Autor autor = new Autor();
                autor.setId(rs.getInt("autor_id"));
                autor.setNombre(rs.getString("autor_nombre"));
                aviso.setAutor(autor);

                // Categoría
                Categoria categoria = new Categoria();
                categoria.setId(rs.getInt("categoria_id"));
                categoria.setNombre(rs.getString("categoria_nombre"));
                aviso.setCategoria(categoria);

                avisos.add(aviso);
            }
        }
        return avisos;
    }

    // Leer aviso por ID
    public Aviso obtenerPorId(int id) throws SQLException {
        String sql = "SELECT a.id, a.titulo, a.descripcion, a.fecha_creacion, a.estado, " +
                "au.id AS autor_id, au.nombre AS autor_nombre, " +
                "c.id AS categoria_id, c.nombre AS categoria_nombre " +
                "FROM aviso a " +
                "INNER JOIN autor au ON a.autor_id = au.id " +
                "INNER JOIN categoria c ON a.categoria_id = c.id " +
                "WHERE a.id = ?";
        try (Connection conn = ConexionBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Aviso aviso = new Aviso();
                    aviso.setId(rs.getInt("id"));
                    aviso.setTitulo(rs.getString("titulo"));
                    aviso.setDescripcion(rs.getString("descripcion"));
                    aviso.setEstado(rs.getString("estado"));

                    Timestamp timestamp = rs.getTimestamp("fecha_creacion");
                    if (timestamp != null) {
                        aviso.setFechaCreacion(timestamp.toLocalDateTime());
                    }

                    Autor autor = new Autor();
                    autor.setId(rs.getInt("autor_id"));
                    autor.setNombre(rs.getString("autor_nombre"));
                    aviso.setAutor(autor);

                    Categoria categoria = new Categoria();
                    categoria.setId(rs.getInt("categoria_id"));
                    categoria.setNombre(rs.getString("categoria_nombre"));
                    aviso.setCategoria(categoria);

                    return aviso;
                }
            }
        }
        return null;
    }

    // Actualizar estado del aviso (pendiente/resuelto)
    public boolean actualizarEstado(int id, String nuevoEstado) throws SQLException {
        String sql = "UPDATE aviso SET estado = ? WHERE id = ?";
        try (Connection conn = ConexionBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Eliminar aviso
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM aviso WHERE id = ?";
        try (Connection conn = ConexionBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}