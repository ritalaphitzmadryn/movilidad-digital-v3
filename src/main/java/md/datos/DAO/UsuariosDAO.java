package md.datos.DAO;

import java.util.Map;
import md.modelo.Usuario;

public interface UsuariosDAO {

    Map<String, Usuario> cargarTodos();

    Usuario buscarPorId(String id);

    void guardar(Usuario usuario);

    void guardarTodos(Map<String, Usuario> usuarios);
}
