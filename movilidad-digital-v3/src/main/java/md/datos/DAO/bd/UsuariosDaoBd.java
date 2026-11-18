package md.datos.DAO.bd;

import md.datos.DAO.UsuariosDAO;
import md.modelo.Usuario;
import md.modelo.Conductor;
import md.modelo.Vehiculo;
import md.modelo.RolUsuario;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UsuariosDaoBd implements UsuariosDAO {

    private final ConexionBD conexion;
    private final VehiculosDaoBd vehiculosDAO;

    // Constructor que recibe la conexión desde la fábrica BdDAOFactory
    public UsuariosDaoBd(ConexionBD conexion) {
        this.conexion = conexion;
        this.vehiculosDAO = new VehiculosDaoBd(conexion);
    }

    @Override
    public Map<String, Usuario> cargarTodos() {
        Map<String, Usuario> usuarios = new HashMap<>();
        
        // 1. Cargamos todos los vehículos directamente (cargarTodos() ya devuelve un Map<String, Vehiculo>)
        Map<String, Vehiculo> mapaVehiculos = vehiculosDAO.cargarTodos();

        // 2. Consultamos la tabla de usuarios de la cátedra
        String sqlUsuarios = "SELECT email, nombre, telefono, licencia_conducir FROM usuario";

        try (Connection conn = conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlUsuarios)) {

            while (rs.next()) {
                String email = rs.getString("email");
                String nombre = rs.getString("nombre");
                String telefono = rs.getString("telefono");
                String licencia = rs.getString("licencia_conducir");

                // Usamos el email como ID
                String id = email; 

                // Instanciamos respetando tu constructor exacto de Usuario
                Usuario usuario = new Usuario(id, nombre, telefono, email);

                // Si tiene licencia registrada, le damos de alta el perfil de conductor
                if (licencia != null && !licencia.trim().isEmpty()) {
                    // Buscamos si tiene un vehículo asociado en la tabla intermedia 'conductor_vehiculo'
                    Vehiculo vehiculoAsociado = buscarVehiculoDeConductor(conn, email, mapaVehiculos);
                    
                    // Invocamos el método de tu clase Usuario
                    usuario.altaConductor(licencia, vehiculoAsociado);
                }

                usuarios.put(email, usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    private Vehiculo buscarVehiculoDeConductor(Connection conn, String email, Map<String, Vehiculo> mapaVehiculos) {
        String sql = "SELECT patente FROM conductor_vehiculo WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String patente = rs.getString("patente");
                    return mapaVehiculos.get(patente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        String sqlUsuario = "INSERT INTO usuario (email, nombre, telefono, licencia_conducir) VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT (email) DO UPDATE SET nombre = EXCLUDED.nombre, telefono = EXCLUDED.telefono, licencia_conducir = EXCLUDED.licencia_conducir";

        try (Connection conn = conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sqlUsuario)) {

            pstmt.setString(1, usuario.getEmail());
            pstmt.setString(2, usuario.getNombre());
            pstmt.setString(3, usuario.getTelefono());
            
            // Usamos getLicenciaConducir() conforme a tu clase Conductor
            if (usuario.getConductor() != null) {
                pstmt.setString(4, usuario.getConductor().getLicenciaConducir());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Usuario buscarPorId(String id) {
        Map<String, Usuario> todos = cargarTodos();
        return todos.get(id);
    }

    @Override
    public void guardarTodos(Map<String, Usuario> usuarios) {
        if (usuarios != null) {
            for (Usuario u : usuarios.values()) {
                guardar(u);
            }
        }
    }
}