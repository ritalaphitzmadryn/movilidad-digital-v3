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

    // Método para obtener la conexión activa
    public Connection conectar() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de PostgreSQL", e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
}