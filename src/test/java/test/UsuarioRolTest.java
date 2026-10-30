package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.TipoVehiculo;
import md.modelo.CategoriaVehiculo;

public class UsuarioRolTest {

    @Test
    public void usuarioNuevoComienzaComoCliente() {
        Usuario usuario = new Usuario("U001","Juan Perez","2804123456","juan@gmail.com");
        assertEquals(RolUsuario.CLIENTE, usuario.getRolActivo());
        assertNull(usuario.getConductor());
        assertNotNull(usuario.getCliente());
    }

    @Test
    public void usuarioPuedeDarseDeAltaComoConductor() {

        Usuario usuario = new Usuario("U001","Juan Perez","2804123456","juan@gmail.com");
        Vehiculo vehiculo = new Vehiculo("AD123BC","Toyota Corolla",4,TipoVehiculo.AUTO,CategoriaVehiculo.PREMIUM);
        usuario.altaConductor("LIC123", vehiculo);
        assertEquals(RolUsuario.CONDUCTOR, usuario.getRolActivo());
        assertNotNull(usuario.getConductor());
        assertEquals("LIC123",usuario.getConductor().getLicenciaConducir());
        assertEquals(vehiculo,usuario.getConductor().getVehiculoActivo());
    }

    @Test
    public void usuarioPuedeCambiarDeConductorACliente() {

        Usuario usuario = new Usuario("U001","Juan Perez","2804123456","juan@gmail.com");
        Vehiculo vehiculo = new Vehiculo("AD123BC","Toyota Corolla",4,TipoVehiculo.AUTO,CategoriaVehiculo.PREMIUM);
        usuario.altaConductor("LIC123", vehiculo);
        usuario.cambiarRolActivo(RolUsuario.CLIENTE);
        assertEquals(RolUsuario.CLIENTE,usuario.getRolActivo());
    }

    @Test
    public void usuarioPuedeCambiarDeClienteAConductor() {
        Usuario usuario = new Usuario("U001","Juan Perez","2804123456","juan@gmail.com");
        Vehiculo vehiculo = new Vehiculo("AD123BC","Toyota Corolla",4,TipoVehiculo.AUTO,CategoriaVehiculo.PREMIUM);
        usuario.altaConductor("LIC123", vehiculo);
        usuario.cambiarRolActivo(RolUsuario.CLIENTE);
        usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        assertEquals(RolUsuario.CONDUCTOR,usuario.getRolActivo());
    }

    @Test
    public void noPuedeCambiarAConductorSinPerfilConductor() {
        Usuario usuario = new Usuario("U001","Juan Perez","2804123456","juan@gmail.com");
        assertThrows(IllegalStateException.class,() -> usuario.cambiarRolActivo(RolUsuario.CONDUCTOR));
    }
}
