package md.datos.DAO.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import md.datos.DAO.*;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;

/**
 * DAO de servicios sobre una base de datos relacional vía JDBC.
 */
public class ServicioDaoBd implements ServicioDAO{

    private final ConexionBD conexion;

    public ServicioDaoBd(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Servicio> cargarTodos() {
        List<Servicio> lista = new ArrayList<>();
        String sql = "SELECT categoriaVehiculo, nombre, precioKm, precioMinuto, tarifaBase, tipoServicio, tipoVehiculo FROM servicio ORDER BY nombre";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
           
        	while (rs.next()) {
                lista.add(new Servicio(
                		rs.getString("nombre"),
                		TipoServicio.valueOf(rs.getString("tipoServicio")),			// Convierte texto a enum
                		TipoVehiculo.valueOf(rs.getString("TipoVehiculo")),			// Convierte texto a enum
                		CategoriaVehiculo.valueOf(rs.getString("categoriaVehiculo")), // Convierte texto a enum
                		rs.getDouble("tarifaBase"),                                // Número decimal
                		rs.getDouble("precioKm"),                                  // Número decimal
                		rs.getDouble("precioMinuto")                               // Número decimal
                ));
            }
        }
        catch (SQLException e) {
        	//Como la interfaz no permite throws SQLException, la envolvemos en RuntimeException
        	throw new RuntimeException("Error al cargar los servicios desde laa base de datos", e); 
        }
        return lista;
    }
    
    @Override
    public Servicio buscarPorNombre(String nombre) {
    	Servicio servicio = null;
    	String sql = "SELECT categoriaVehiculo, nombre, precioKm, precioMinuto, tarifaBase, tipoServicio, tipoVehiculo FROM servicio WHERE nombre = ?";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)){
        		
        	// 1. Asignamos el parámetro al signo de interrogación (?) de la consulta
            ps.setString(1, nombre);
        
        	try (ResultSet rs = ps.executeQuery()) {
        		// 2. Usamos 'if' porque esperamos como máximo un solo resultado
        		if (rs.next()) {
        			servicio = new Servicio (
                		rs.getString("nombre"),
                		TipoServicio.valueOf(rs.getString("tipoServicio")),			// Convierte texto a enum
                		TipoVehiculo.valueOf(rs.getString("TipoVehiculo")),			// Convierte texto a enum
                		CategoriaVehiculo.valueOf(rs.getString("categoriaVehiculo")), // Convierte texto a enum
                		rs.getDouble("tarifaBase"),                                // Número decimal
                		rs.getDouble("precioKm"),                                  // Número decimal
                		rs.getDouble("precioMinuto")                               // Número decimal
        			);
        		}
        	}
        }
        catch (SQLException e) {
        	throw new RuntimeException("Error al buscar el servicio por nombre"); 
        }
        // 3. Retornamos el objeto (si no lo encontró, devolverá null)
        return servicio;
    }
    
    @Override
    public void guardar(Servicio servicio) {
        String sql = "INSERT INTO servicio (categoriaVehiculo, nombre, precioKm, "
        				+ "precioMinuto, tarifaBase, tipoServicio, tipoVehiculo)";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)){
        		
        	// 1. Asignamos cada atributo del servicio al signo '?' correspondiente
        	//Los enum se guardan convirtiéndolos a texto .name() (para que coincidan con el valueOf que usamos al leer.
            ps.setString(1, servicio.getCategoriaVehiculo().name());
            ps.setString(2, servicio.getNombre());
            ps.setDouble(3, servicio.getValorPorKm());
            ps.setDouble(4, servicio.getValorPorMinuto());
            ps.setDouble(5,  servicio.getTarifaBase());
            ps.setString(6,  servicio.getTipoServicio().name());
            ps.setString(7, servicio.getTipoVehiculo().name());
            
            ps.executeUpdate();
        }
        catch (SQLException e){
        	throw new RuntimeException("Error al guardar el servicio: "+ servicio.getNombre(), e); 
        }
    }
    
    @Override
    public void guardarTodos(List<Servicio> servicios) {
        for (Servicio servicio : servicios) {
        	guardar(servicio);
        }
    }
}