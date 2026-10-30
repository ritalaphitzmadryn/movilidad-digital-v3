package md.datos.DAO.txt;

import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;

public class TxtDAOFactory implements DAOFactory {

    @Override
    public UsuariosDAO getUsuarioDAO() {
        return new UsuariosDaoTxt();
    }

    @Override
    public VehiculosDAO getVehiculoDAO() {
        return new VehiculosDaoTxt();
    }

    @Override
    public ServicioDAO getServicioDAO() {
        return new ServicioDaoTxt();
    }
}