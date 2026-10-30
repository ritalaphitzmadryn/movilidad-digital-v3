package md.datos.DAO.bd;

import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;

public class BdDAOFactory implements DAOFactory {
	
	private final ConexionBD conexion;
	
	// Recibe la conexión ya configurada por parámetro
    public BdDAOFactory(ConexionBD conexion) {
        this.conexion = conexion;
    }
	
	@Override
	public ServicioDAO getServicioDAO() {
		  return new ServicioDaoBd(conexion);
	  }
	@Override
	public UsuariosDAO getUsuarioDAO() {
		  return new UsuariosDaoBd(conexion);
	  }
	@Override
	public VehiculosDAO getVehiculoDAO() {
		  return new VehiculosDaoBd(conexion);	  
	  }

}
