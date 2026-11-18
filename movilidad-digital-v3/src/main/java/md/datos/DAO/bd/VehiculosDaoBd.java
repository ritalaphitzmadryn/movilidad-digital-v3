package md.datos.DAO.bd;

import md.datos.DAO.VehiculosDAO;
import md.modelo.Vehiculo;
import md.modelo.TipoVehiculo;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VehiculosDaoBd implements VehiculosDAO {

    private final ConexionBD conexion;

    // Constructor que recibe la conexión desde la fábrica o desde UsuariosDaoBd
    public VehiculosDaoBd(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public Map<String, Vehiculo> cargarTodos() {
        Map<String, Vehiculo> vehiculos = new HashMap<>();
        String sql = "SELECT patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios FROM vehiculo";

        try (Connection conn = conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String patente = rs.getString("patente");
                String modelo = rs.getString("modelo");
                int capacidad = rs.getInt("capacidad_pasajeros");
                int dbTipoVehiculo = rs.getInt("tipo_vehiculo");
                int dbCategoriaVehiculo = rs.getInt("categoria_vehiculo");
                int mascaraServicios = rs.getInt("tipo_servicios");

                // 1. Mapear entero de la BD a TipoVehiculo (0 = AUTO, 1 = MOTO)
                TipoVehiculo tipoVehiculo = (dbTipoVehiculo == 1) ? TipoVehiculo.MOTO : TipoVehiculo.AUTO;

                // 2. Mapear entero de la BD a CategoriaVehiculo
                CategoriaVehiculo categoriaVehiculo;
                switch (dbCategoriaVehiculo) {
                    case 1:
                        categoriaVehiculo = CategoriaVehiculo.CONFORT;
                        break;
                    case 2:
                        categoriaVehiculo = CategoriaVehiculo.PREMIUM;
                        break;
                    default:
                        categoriaVehiculo = CategoriaVehiculo.ESTANDAR;
                        break;
                }

                // 3. Instanciar el vehículo usando tu constructor exacto
                Vehiculo vehiculo = new Vehiculo(patente, modelo, capacidad, tipoVehiculo, categoriaVehiculo);

                // 4. Desempaquetar la máscara de bits de servicios (1 = PASAJEROS, 2 = ENVIOS)
                if ((mascaraServicios & 1) != 0) {
                    vehiculo.agregarTipoServicio(TipoServicio.PASAJEROS);
                }
                if ((mascaraServicios & 2) != 0) {
                    vehiculo.agregarTipoServicio(TipoServicio.ENVIOS);
                }

                // Guardamos en el Map usando la patente como clave
                vehiculos.put(patente, vehiculo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehiculos;
    }

    @Override
    public Vehiculo buscarPorPatente(String patente) {
        String sql = "SELECT patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios FROM vehiculo WHERE patente = ?";
        
        try (Connection conn = conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String modelo = rs.getString("modelo");
                    int capacidad = rs.getInt("capacidad_pasajeros");
                    int dbTipoVehiculo = rs.getInt("tipo_vehiculo");
                    int dbCategoriaVehiculo = rs.getInt("categoria_vehiculo");
                    int mascaraServicios = rs.getInt("tipo_servicios");

                    TipoVehiculo tipoVehiculo = (dbTipoVehiculo == 1) ? TipoVehiculo.MOTO : TipoVehiculo.AUTO;

                    CategoriaVehiculo categoriaVehiculo;
                    switch (dbCategoriaVehiculo) {
                        case 1: categoriaVehiculo = CategoriaVehiculo.CONFORT; break;
                        case 2: categoriaVehiculo = CategoriaVehiculo.PREMIUM; break;
                        default: categoriaVehiculo = CategoriaVehiculo.ESTANDAR; break;
                    }

                    Vehiculo vehiculo = new Vehiculo(patente, modelo, capacidad, tipoVehiculo, categoriaVehiculo);

                    if ((mascaraServicios & 1) != 0) vehiculo.agregarTipoServicio(TipoServicio.PASAJEROS);
                    if ((mascaraServicios & 2) != 0) vehiculo.agregarTipoServicio(TipoServicio.ENVIOS);

                    return vehiculo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void guardar(Vehiculo vehiculo) {
        String sql = "INSERT INTO vehiculo (patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (patente) DO UPDATE SET modelo = EXCLUDED.modelo, capacidad_pasajeros = EXCLUDED.capacidad_pasajeros, " +
                     "tipo_vehiculo = EXCLUDED.tipo_vehiculo, categoria_vehiculo = EXCLUDED.categoria_vehiculo, tipo_servicios = EXCLUDED.tipo_servicios";

        try (Connection conn = conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehiculo.getPatente());
            pstmt.setString(2, vehiculo.getModelo());
            pstmt.setInt(3, vehiculo.getCapacidadPasajeros());

            int dbTipo = (vehiculo.getTipoVehiculo() == TipoVehiculo.MOTO) ? 1 : 0;
            pstmt.setInt(4, dbTipo);

            int dbCat = 0;
            if (vehiculo.getCategoriaVehiculo() == CategoriaVehiculo.CONFORT) dbCat = 1;
            else if (vehiculo.getCategoriaVehiculo() == CategoriaVehiculo.PREMIUM) dbCat = 2;
            pstmt.setInt(5, dbCat);

            int mascara = 0;
            for (TipoServicio ts : vehiculo.getTiposServicios()) {
                if (ts == TipoServicio.PASAJEROS) mascara |= 1;
                if (ts == TipoServicio.ENVIOS) mascara |= 2;
            }
            pstmt.setInt(6, mascara);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void guardarTodos(Map<String, Vehiculo> vehiculos) {
        if (vehiculos != null) {
            for (Vehiculo v : vehiculos.values()) {
                guardar(v);
            }
        }
    }
}