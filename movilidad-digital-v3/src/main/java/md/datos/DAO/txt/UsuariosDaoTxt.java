package md.datos.DAO.txt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import md.config.ConfiguracionUsuarios;
import md.datos.DAO.UsuariosDAO;
import md.modelo.Usuario;
import md.modelo.Vehiculo;

public class UsuariosDaoTxt implements UsuariosDAO {

    @Override
    public Map<String, Usuario> cargarTodos() {
        // Obtenemos los vehículos existentes a través de su DAO para que los conductores puedan vincularlos
        VehiculosDaoTxt vehiculosDao = new VehiculosDaoTxt();
        Map<String, Vehiculo> vehiculos = vehiculosDao.cargarTodos();
        
        return ConfiguracionUsuarios.cargarUsuarios("usuarios.txt", vehiculos);
    }

    @Override
    public Usuario buscarPorId(String id) {
        Map<String, Usuario> usuarios = cargarTodos();
        return usuarios.get(id);
    }

    @Override
    public void guardar(Usuario usuario) {
        Map<String, Usuario> usuarios = cargarTodos();
        usuarios.put(usuario.getId(), usuario);
        guardarTodos(usuarios);
    }

    @Override
    public void guardarTodos(Map<String, Usuario> usuarios) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("usuarios.txt"))) {
            bw.write("#nombre;telefono;email;licenciaConducir;patente;...");
            bw.newLine();
            for (Usuario u : usuarios.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(u.getNombre()).append(";")
                  .append(u.getTelefono()).append(";")
                  .append(u.getEmail()).append(";");
                
                // Si el usuario es conductor, agregamos su licencia y patentes asociadas
                if (u.getConductor() != null) {
                    sb.append(u.getConductor().getLicenciaConducir()).append(";");
                    if (u.getConductor().getVehiculos() != null) {
                        for (Vehiculo v : u.getConductor().getVehiculos()) {
                            sb.append(v.getPatente()).append(";");
                        }
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar usuarios en archivo txt", e);
        }
    }
}