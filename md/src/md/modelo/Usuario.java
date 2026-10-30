package md.modelo;

public class Usuario {
    private String id;
    private String nombre;
    private String telefono;
    private String email;
    private RolUsuario rolActivo;
    private Cliente cliente;
    private Conductor conductor;

    public Usuario(String id, String nombre, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.cliente = new Cliente(); // Todo usuario se crea como cliente por defecto
        this.rolActivo = RolUsuario.CLIENTE;
        this.conductor = null;
    }

    public void altaConductor(String licencia, Vehiculo vehiculo) {
        this.conductor = new Conductor(licencia, vehiculo);
        this.rolActivo = RolUsuario.CONDUCTOR;
    }

    public void cambiarRolActivo(RolUsuario rolNuevo) {
        if (rolNuevo == RolUsuario.CONDUCTOR && this.conductor == null) {
            throw new IllegalStateException("El usuario no está registrado como conductor.");
        }
        this.rolActivo = rolNuevo;
    }

    public String getId() { return id;  }
    public String getNombre() {  return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public RolUsuario getRolActivo() { return rolActivo; }
    public Cliente getCliente() { return cliente; }
    public Conductor getConductor() {  return conductor; }
}