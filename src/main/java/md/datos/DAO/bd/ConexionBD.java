package md.datos.DAO.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Datos de conexión JDBC compartidos por los DAO de la familia SQL.
 * Abrir una conexión requiere URL, credenciales y un driver compatibles con el servidor.
 * MySQL y PostgreSQL utilizan esta misma clase y DAO JDBC. Otro gestor exige
 * comprobar también la compatibilidad del esquema, las consultas y los tipos;
 * la presencia de un driver por sí sola no garantiza esa compatibilidad.
 */
public class ConexionBD {

    private final String url;
    private final String usuario;
    private final String password;

    public ConexionBD(String url, String usuario, String password) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }
}