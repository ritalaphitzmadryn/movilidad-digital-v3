package md.datos.DAO.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import md.datos.DAO.UsuariosDAO;
import md.modelo.RolUsuario;
import md.modelo.Usuario;

public class UsuariosDaoBd implements UsuariosDAO {

    private final ConexionBD conexion;

    public UsuariosDaoBd(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public Map<String, Usuario> cargarTodos() {
        Map<String, Usuario> mapa = new HashMap<>();
        String sql = "SELECT id, nombre, telefono, email, rolActivo FROM usuario";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
           
            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getString("email")
                );
                
             // Mapeamos el rol activo respetando las restricciones de negocio
                String rolStr = rs.getString("rolActivo");
                if (rolStr != null) {
                    RolUsuario rol = RolUsuario.valueOf(rolStr);
                    if (rol == RolUsuario.CONDUCTOR) {
                        // Si es conductor, la regla de Usuario exige inicializarlo antes
                        // usuario.altaConductor("LICENCIA_EJEMPLO", vehiculoEjemplo);
                    }
                    usuario.cambiarRolActivo(rol);
                }
                
                // Usamos el id como la clave del Map y el objeto como valor
                mapa.put(usuario.getId(), usuario);
            }
        } 
        catch (SQLException e) {
            throw new RuntimeException("Error al cargar los usuarios desde la base de datos", e); 
        }
        return mapa;
    }

    @Override
    public Usuario buscarPorId(String id) {
        Usuario usuario = null;
        String sql = "SELECT id, nombre, telefono, email, rolActivo FROM usuario WHERE id = ?";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
                
            ps.setString(1, id);
        
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email")
                    );
                    String rolStr = rs.getString("rolActivo");
                    if (rolStr != null) {
                        RolUsuario rol = RolUsuario.valueOf(rolStr);
                        if (rol == RolUsuario.CONDUCTOR) {
                            // usuario.altaConductor("LICENCIA_EJEMPLO", vehiculoEjemplo);
                        }
                        usuario.cambiarRolActivo(rol);
                    }
                }
            }
        } 
        catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario por ID: " + id, e); 
        }
        return usuario;
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (id, nombre, telefono, email, rolActivo) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, usuario.getId());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getTelefono());
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getRolActivo().name());
            
            ps.executeUpdate();
        } 
        catch (SQLException e) {
            throw new RuntimeException("Error al guardar el usuario: " + usuario.getId(), e); 
        }
    }

    @Override
    public void guardarTodos(Map<String, Usuario> usuarios) {
        for (Usuario usuario : usuarios.values()) {
            guardar(usuario);
        }
    }
}
