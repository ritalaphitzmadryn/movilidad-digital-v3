package md.datos.DAO.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private String url;
    private String user;
    private String password;

    // Constructor que requiere DAOLoader
    public ConexionBD(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // Método único para obtener la conexión activa
    public Connection conectar() {
        try {
            // Forzamos la carga explícita del driver para que Java lo reconozca
            Class.forName("org.postgresql.Driver");
            
            // Usamos 'user' (que es el nombre correcto del atributo)
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error: No se encontró el driver JDBC de PostgreSQL en el classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar con la base de datos de la facultad.", e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
}