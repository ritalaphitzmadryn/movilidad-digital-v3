package md.datos.DAO.txt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import md.config.ConfiguracionServicios;
import md.datos.DAO.ServicioDAO;
import md.modelo.Servicio;

public class ServicioDaoTxt implements ServicioDAO {

    @Override
    public List<Servicio> cargarTodos() {
        return ConfiguracionServicios.cargarServicios("servicios.txt");
    }

    @Override
    public Servicio buscarPorNombre(String nombre) {
        List<Servicio> servicios = cargarTodos();
        for (Servicio s : servicios) {
            if (s.getNombre() != null && s.getNombre().equalsIgnoreCase(nombre)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void guardar(Servicio servicio) {
        List<Servicio> servicios = cargarTodos();
        servicios.add(servicio);
        guardarTodos(servicios);
    }

    @Override
    public void guardarTodos(List<Servicio> servicios) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("servicios.txt"))) {
            bw.write("#nombre;tarifaBase;precioKm;precioMinuto;TipoVehiculo;CategoriaVehiculo;TipoServicio;");
            bw.newLine();
            for (Servicio s : servicios) {
                StringBuilder sb = new StringBuilder();
                sb.append(s.getNombre()).append(";")
                  .append(s.getTarifaBase()).append(";")
                  .append(s.getValorPorKm()).append(";")
                  .append(s.getValorPorMinuto()).append(";")
                  .append(s.getTipoVehiculo()).append(";")
                  .append(s.getCategoriaVehiculo()).append(";")
                  .append(s.getTipoServicio()).append(";");
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar servicios en archivo txt", e);
        }
    }
}