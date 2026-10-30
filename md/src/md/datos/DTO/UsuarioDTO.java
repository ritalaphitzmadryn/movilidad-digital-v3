package md.datos.DTO;

import md.modelo.Cliente;
import md.modelo.Conductor;
import md.modelo.RolUsuario;

public class UsuarioDTO {

    private String id;
    private String nombre;
    private String telefono;
    private String email;
    private RolUsuario rolActivo;
    private Cliente cliente;
    private Conductor conductor;

    public UsuarioDTO(String id, String nombre, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.cliente = new Cliente(); // Todo usuario se crea como cliente por defecto
        this.rolActivo = RolUsuario.CLIENTE;
        this.conductor = null;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public RolUsuario getRolActivo() {
		return rolActivo;
	}

	public void setRolActivo(RolUsuario rolActivo) {
		this.rolActivo = rolActivo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Conductor getConductor() {
		return conductor;
	}

	public void setConductor(Conductor conductor) {
		this.conductor = conductor;
	}
}
