package md.datos.DAO.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import md.datos.DAO.VehiculosDAO;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoVehiculo;
import md.modelo.Vehiculo;

public class VehiculosDaoBd implements VehiculosDAO {
	
	private final ConexionBD conexion;

	public VehiculosDaoBd(ConexionBD conexion) {
		this.conexion = conexion;
	}
	    
	@Override
	public Map<String, Vehiculo> cargarTodos() {
        Map<String, Vehiculo> mapa = new HashMap<>();
        String sql = "SELECT patente, modelo, capacidadPasajeros, tipoVehiculo, categoriaVehiculo FROM vehiculo";
	        
        try (Connection c = conexion.obtenerConexion();
        	PreparedStatement ps = c.prepareStatement(sql);
        	ResultSet rs = ps.executeQuery()) {
		
        	while (rs.next()) {
        		Vehiculo vehiculo = new Vehiculo(
        			rs.getString("patente"),
        			rs.getString("modelo"),
        			rs.getInt("capacidadPasajeros"),
        			TipoVehiculo.valueOf(rs.getString("tipoVehiculo")),			// Convierte texto a enum
        			CategoriaVehiculo.valueOf(rs.getString("categoriaVehiculo")) // Convierte texto a enum
        		);
        		// Usamos la patente como la clave del Map y el objeto como valor
        		mapa.put(vehiculo.getPatente(), vehiculo);
        	}
        }
        catch (SQLException e) {
        	throw new RuntimeException("Error al cargar los vehiculos desde la base de datos", e);
        }
        return mapa;
	}
	@Override
	public Vehiculo buscarPorPatente(String patente) {
	    Vehiculo vehiculo = null;
	 // 1. Agregamos el WHERE patente = ? para poder filtrar por el parámetro
    	String sql = "SELECT patente, modelo, capacidadPasajeros, tipoVehiculo, categoriaVehiculo FROM vehiculo WHERE patente = ?";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)){
        		
        	// 1. Asignamos el parámetro al signo de interrogación (?) de la consulta
            ps.setString(1, patente);
        
        	try (ResultSet rs = ps.executeQuery()) {
        		// 2. Usamos 'if' porque esperamos como máximo un solo resultado
        		
        		if (rs.next()) {
        			vehiculo = new Vehiculo (
                		rs.getString("patente"),
                		rs.getString("modelo"),
                		rs.getInt("capacidadPasajeros"),
                		TipoVehiculo.valueOf(rs.getString("TipoVehiculo")),			// Convierte texto a enum
                		CategoriaVehiculo.valueOf(rs.getString("categoriaVehiculo")) // Convierte texto a enum
        			);
        		}
        	}
        }
        catch (SQLException e) {
        	throw new RuntimeException("Error al buscar el vehículo: " + patente, e);
        }
        return vehiculo;
    }

	@Override
    public void guardar(Vehiculo vehiculo) {
        String sql = "INSERT INTO vehiculo (patente, modelo, capacidadPasajeros, tipoVehiculo, categoriaVehiculo) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, vehiculo.getPatente());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getCapacidadPasajeros());
            ps.setString(4, vehiculo.getTipoVehiculo().name());
            ps.setString(5, vehiculo.getCategoriaVehiculo().name());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el vehículo: " + vehiculo.getPatente(), e); 
        }
    }

    @Override
    public void guardarTodos(Map<String, Vehiculo> vehiculos) {
        // Recorremos los valores del mapa para ir guardándolos de a uno reutilizando el método guardar
        for (Vehiculo vehiculo : vehiculos.values()) {
            guardar(vehiculo);
        }
    }
}
