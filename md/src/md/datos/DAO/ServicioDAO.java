package md.datos.DAO;
import java.util.List;

import md.modelo.Servicio;

public interface ServicioDAO {

    List<Servicio> cargarTodos();

    Servicio buscarPorNombre(String nombre);

    void guardar(Servicio servicio);

    void guardarTodos(List<Servicio> servicios);
}