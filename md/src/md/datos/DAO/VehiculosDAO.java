package md.datos.DAO;

import java.util.Map;
import md.modelo.Vehiculo;

public interface VehiculosDAO {

    Map<String, Vehiculo> cargarTodos();

    Vehiculo buscarPorPatente(String patente);

    void guardar(Vehiculo vehiculo);

    void guardarTodos(Map<String, Vehiculo> vehiculos);
}
