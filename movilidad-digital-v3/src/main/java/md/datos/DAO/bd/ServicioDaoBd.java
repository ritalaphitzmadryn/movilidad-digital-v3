package md.datos.DAO.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import md.datos.DAO.*;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Usuario;
import md.modelo.Viaje;
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
        
        try (Connection c = conexion.conectar();
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
        
        try (Connection c = conexion.conectar();
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
        
        try (Connection c = conexion.conectar();
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
    public List<Viaje> cargarViajesDisponiblesDesdeBd() {
        List<Viaje> lista = new ArrayList<>();
        String sql = "SELECT id, origen_nombre, destino_nombre, estado FROM viaje WHERE estado = 'SOLICITADO'"; 

        try (Connection conn = conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Creamos ubicaciones ficticias o las leemos de la BD según tu estructura
                md.modelo.Ubicacion origen = new md.modelo.Ubicacion(rs.getString("origen_nombre"), 0.0, 0.0);
                md.modelo.Ubicacion destino = new md.modelo.Ubicacion(rs.getString("destino_nombre"), 0.0, 0.0);
                
                // Usamos el constructor válido de Viaje (Cliente, Servicio, Origen, Destino)
                // Nota: Si el cliente o servicio vienen de la BD, deberás consultarlos o pasarlos adecuadamente.
                Viaje viaje = new Viaje(null, null, origen, destino);
                lista.add(viaje);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    public List<Viaje> cargarHistorialPorConductor(String emailConductor) {
        List<Viaje> lista = new ArrayList<>();
        // Especificamos el esquema movilidad_digital.servicio
        String sql = "SELECT nombre, tarifa_base FROM movilidad_digital.servicio"; 

        try (Connection c = conexion.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String descripcionRuta = rs.getString("nombre");
                double costo = rs.getDouble("tarifa_base");
                
                md.modelo.Ubicacion origen = new md.modelo.Ubicacion(descripcionRuta, 0.0, 0.0);
                md.modelo.Ubicacion destino = new md.modelo.Ubicacion("Destino Registrado", 0.0, 0.0);
                
                Usuario clienteFicticio = new Usuario(); 
                Viaje viaje = new Viaje(clienteFicticio, null, origen, destino);
                
                lista.add(viaje);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar el historial de servicios", e);
        }
        return lista;
    }
    
    public void guardarViajeRealizado(md.modelo.Viaje viaje, String emailConductor) {
        // Especificamos el esquema movilidad_digital.servicio
        String sql = "INSERT INTO movilidad_digital.servicio (nombre, tarifa_base, precio_km, precio_minuto, tipo_vehiculo, categoria_vehiculo, tipo_servicio) VALUES (?, ?, ?, ?, 1, 1, 1)";

        try (Connection c = conexion.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            String origenStr = viaje.getOrigen() != null ? viaje.getOrigen().getDireccion() : "Origen";
            String destinoStr = viaje.getDestino() != null ? viaje.getDestino().getDireccion() : "Destino";
            
            // Armamos un texto compacto y nos aseguramos de que no supere los 50 caracteres
            String descripcion = origenStr + " -> " + destinoStr;
            if (descripcion.length() > 50) {
                descripcion = descripcion.substring(0, 47) + "...";
            }
            
            ps.setString(1, descripcion);
            ps.setDouble(2, viaje.getCostoCalculado());
            ps.setDouble(3, 0.0);
            ps.setDouble(4, 0.0);
            
            ps.executeUpdate();
            System.out.println("💾 [DAO] ¡Viaje guardado exitosamente en la tabla de servicios!");
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el viaje realizado en servicios", e);
        }
    }    
}