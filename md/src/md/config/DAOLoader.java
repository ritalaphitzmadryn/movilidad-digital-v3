package md.config;

import md.datos.*;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.bd.*;
import md.datos.DAO.txt.TxtDAOFactory;
import md.datos.DAO.bd.BdDAOFactory;

/**
 * Selecciona la fábrica de persistencia según la configuración.
 *
 * DAOLoader centraliza la selección entre la persistencia
 * mediante archivos de texto y mediante base de datos.
 */
public class DAOLoader {

    public static DAOFactory getFactory() {

    	Configuracion config = new Configuracion("config.properties");
    	String tipo = config.getPersistencia();
    	
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalStateException(
                "No se ha configurado el tipo de persistencia."
            );
        }

        switch (tipo.trim().toLowerCase()) {

            case "archivo":
                return new TxtDAOFactory();

            case "bd":
               // 1. Obtenemos los datos de conexión desde la configuración
               ConexionBD conexion = new ConexionBD(
            		   config.getUrl(),
                       config.getUsuarioBd(),
                       config.getPassword()   
               );   
               // 2. Inyectamos la conexión en la fábrica de base de datos
               return new BdDAOFactory(conexion);

            default:
                throw new IllegalStateException(
                    "Tipo de persistencia no soportado: " + tipo
                );
        }
    }
}
