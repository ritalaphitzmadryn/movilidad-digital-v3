package md.datos.DAO;


public interface DAOFactory {

    UsuariosDAO getUsuarioDAO();

    VehiculosDAO getVehiculoDAO();

    ServicioDAO getServicioDAO();
}



