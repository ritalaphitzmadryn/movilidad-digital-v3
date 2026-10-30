package md.datos.DAO.txt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import md.datos.DAO.VehiculosDAO;
import md.datos.ConfiguracionVehiculos;
import md.modelo.TipoServicio;
import md.modelo.Vehiculo;

public class VehiculosDaoTxt implements VehiculosDAO {

    @Override
    public Map<String, Vehiculo> cargarTodos() {
        return ConfiguracionVehiculos.cargarVehiculos("vehiculos.txt");
    }

    @Override
    public Vehiculo buscarPorPatente(String patente) {
        Map<String, Vehiculo> vehiculos = cargarTodos();
        return vehiculos.get(patente);
    }

    @Override
    public void guardar(Vehiculo vehiculo) {
        Map<String, Vehiculo> vehiculos = cargarTodos();
        vehiculos.put(vehiculo.getPatente(), vehiculo);
        guardarTodos(vehiculos);
    }

    @Override
    public void guardarTodos(Map<String, Vehiculo> vehiculos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("vehiculos.txt"))) {
            bw.write("#patente;modelo;capacidadPasajeros;TipoVehiculo;CategoriaVehiculo;TipoServicio;...");
            bw.newLine();
            for (Vehiculo v : vehiculos.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(v.getPatente()).append(";")
                  .append(v.getModelo()).append(";")
                  .append(v.getCapacidadPasajeros()).append(";")
                  .append(v.getTipoVehiculo()).append(";")
                  .append(v.getCategoriaVehiculo()).append(";");
                
                if (v.getTiposServicios() != null) {
                    for (TipoServicio ts : v.getTiposServicios()) {
                        sb.append(ts.name()).append(";");
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar vehículos en archivo txt", e);
        }
    }
}