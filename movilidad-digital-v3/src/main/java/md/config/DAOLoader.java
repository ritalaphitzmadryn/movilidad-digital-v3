package md.config;

import md.datos.DAO.DAOFactory;
import md.datos.DAO.bd.BdDAOFactory;
import md.datos.DAO.bd.ConexionBD;
import md.datos.DAO.txt.TxtDAOFactory;

/**
 * Selecciona la fábrica de persistencia según la configuración.
 *
 * DAOLoader centraliza la selección entre la persistencia
 * mediante archivos de texto y mediante base de datos.
 */
public class DAOLoader {

    public static DAOFactory getFactory() {
        // Como CargaParametros es estático, leemos directamente el tipo de persistencia
        // (por defecto asumimos "archivo" o "bd" según lo que tengas en tu application.properties)
        String tipo = CargaParametros.getProperty("app.persistencia", "archivo");
        
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalStateException(
                "No se ha configurado el tipo de persistencia."
            );
        }

        switch (tipo.trim().toLowerCase()) {

            case "archivo":
            case "txt":
                return new TxtDAOFactory();

            case "bd":
            case "database":
                // 1. Obtenemos los parámetros de conexión de la configuración estática
                String url = CargaParametros.getProperty("db.url", "jdbc:postgresql://localhost:5432/movilidad_digital");
                String user = CargaParametros.getProperty("db.usuario", "postgres");
                String pass = CargaParametros.getProperty("db.password", "");

                ConexionBD conexion = new ConexionBD(url, user, pass);
                
                // 2. Inyectamos la conexión en la fábrica de base de datos
                return new BdDAOFactory(conexion);

            default:
                throw new IllegalStateException(
                    "Tipo de persistencia no soportado: " + tipo
                );
        }
    }
}