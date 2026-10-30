package md.logica;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import md.modelo.CalificacionViaje;
import md.modelo.Cliente;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.RegistroViaje;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import md.modelo.Ubicacion;


public class GestorMovilidad {

    // =========================
    // ESTRUCTURAS DE DATOS
    // =========================

    private Map<String, Usuario> usuarios;
    private Map<String, Cliente> clientes;
    private Map<String, Conductor> conductores;
    private Map<String, Vehiculo> vehiculos;
    private Map<UUID, Viaje> viajes;
    private List<Servicio> servicios;


    // =========================
    // CONSTRUCTOR
    // =========================

    public GestorMovilidad() {

        usuarios = new HashMap<>();
        clientes = new HashMap<>();
        conductores = new HashMap<>();
        vehiculos = new HashMap<>();
        viajes = new HashMap<>();
        servicios = new ArrayList<>();
        
    }

    // =========================
    // REGISTRO DE CLIENTES
    // =========================

    public void registrarCliente(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                "El usuario no puede ser null"
            );
        }

        if (usuario.getCliente() == null) {
            throw new IllegalArgumentException(
                "El usuario no tiene un perfil de cliente"
            );
        }

        usuarios.putIfAbsent(usuario.getId(), usuario);
        clientes.put(usuario.getId(), usuario.getCliente());
    }
    // =========================
    // REGISTRO DE CONDUCTORES
    // =========================
    public void registrarConductor(Usuario usuario) {
        // Validaciones básicas de seguridad
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser null");
        }
        if (usuario.getConductor() == null) {
            throw new IllegalArgumentException("El usuario no tiene un perfil de conductor");
        }

        Conductor conductor = usuario.getConductor();
        String licencia = conductor.getLicenciaConducir();

        // Si el conductor ya está registrado (por su licencia), lo ignoramos silenciosamente
        if (conductores.containsKey(licencia)) {
            return; 
        }

        // Si no está registrado, lo agregamos normalmente
        usuarios.putIfAbsent(usuario.getId(), usuario);
        conductores.put(licencia, conductor);
    }
    // =========================
    // BUSCAR CONDUCTOR DISPONIBLE
    // =========================
    public Conductor buscarConductorDisponible() {
        for (Conductor conductor : conductores.values()) {
            if (conductor.getEstadoConductor() == EstadoConductor.DISPONIBLE) {
                return conductor;
            }
        }
        return null;
    }
    // =========================
    // SOLICITAR VIAJE
    // =========================
    public Viaje solicitarViaje(Usuario cliente,Servicio servicio,Ubicacion origen,Ubicacion destino,LocalDateTime fechaHora) {
        if (cliente == null) {
            throw new IllegalArgumentException(
                "El cliente no puede ser null"
            );
        }
        if (cliente.getCliente() == null) {
            throw new IllegalArgumentException(
                "El usuario no tiene perfil de cliente"
            );
        }
        if (servicio == null) {
            throw new IllegalArgumentException(
                "El servicio no puede ser null"
            );
        }
        List<RegistroViaje> registros = new ArrayList<>();
        Viaje viaje = new Viaje(cliente,servicio,origen,destino, registros);
        viaje.solicitar(fechaHora);
        viajes.put(viaje.getId(), viaje);
        cliente.getCliente().agregarViaje(viaje);
        return viaje;
    }
    // =========================
    // ACEPTAR VIAJE
    // =========================
    public void aceptarViaje(UUID idViaje,Usuario conductor,LocalDateTime fechaHora) {
        Viaje viaje = obtenerViaje(idViaje);
        if (viaje == null) {
            throw new IllegalArgumentException(
                "No existe el viaje solicitado"
            );
        }
        if (conductor == null) {
            throw new IllegalArgumentException(
                "El conductor no puede ser null"
            );
        }
        if (conductor.getConductor() == null) {
            throw new IllegalArgumentException(
                "El usuario no tiene perfil de conductor"
            );
        }
        viaje.aceptar(fechaHora, conductor);
        conductor.getConductor().agregarViaje(viaje);
    }
    // =========================
    // RECHAZAR VIAJE
    // =========================
    public void rechazarViaje(UUID idViaje,LocalDateTime fechaHora,String motivo) {
        Viaje viaje = obtenerViaje(idViaje);
        if (viaje == null) {
            throw new IllegalArgumentException(
                "No existe el viaje"
            );
        }
        viaje.rechazar(fechaHora);
    }
    // =========================
    // INICIAR VIAJE
    // =========================
    public void iniciarViaje(UUID idViaje,LocalDateTime fechaHora) {
        Viaje viaje = obtenerViaje(idViaje);
        if (viaje == null) {
            throw new IllegalArgumentException(
                "No existe el viaje"
            );
        }
        viaje.iniciar(fechaHora);
    }
    // =========================
    // FINALIZAR VIAJE
    // =========================
    public void finalizarViaje(UUID idViaje,LocalDateTime fechaHora,CalificacionViaje calificacionConductor,CalificacionViaje calificacionCliente) {
        Viaje viaje = obtenerViaje(idViaje);
        if (viaje == null) {
            throw new IllegalArgumentException(
                "No existe el viaje"
            );
        }
        viaje.finalizar(fechaHora,calificacionConductor,calificacionCliente);
    }
    // =========================
    // CANCELAR VIAJE
    // =========================
    public void cancelarViaje(UUID idViaje,LocalDateTime fechaHora,String motivo, Usuario usuario) {
        Viaje viaje = obtenerViaje(idViaje);
        if (viaje == null) {
            throw new IllegalArgumentException(
                "No existe el viaje"
            );
        }
        viaje.cancelar(fechaHora, usuario, motivo);
    }
    // =========================
    // OBTENER VIAJE
    // =========================
    public Viaje obtenerViaje(UUID idViaje) {
        return viajes.get(idViaje);
    }
    // =========================
    // OBTENER VIAJES DEL CLIENTE
    // =========================
    public List<Viaje> obtenerViajes(Cliente cliente) {
        List<Viaje> resultado = new ArrayList<>();
        for (Viaje viaje : viajes.values()) {
            if (viaje.getCliente().equals(cliente)) {
                resultado.add(viaje);
            }
        }
        return resultado;
    }
    // =========================
    // OBTENER VIAJES DEL CONDUCTOR
    // =========================
    public List<Viaje> obtenerViajes(Conductor conductor) {
        List<Viaje> resultado = new ArrayList<>();
        for (Viaje viaje : viajes.values()) {
            if (viaje.getConductor() != null &&
                viaje.getConductor().equals(conductor)) {
                resultado.add(viaje);
            }
        }
        return resultado;
    }
    // =========================
    // OBTENER CONDUCTORES
    // =========================
    public List<Conductor> obtenerConductoresDisponibles() {
        List<Conductor> disponibles = new ArrayList<>();
        for (Conductor conductor : conductores.values()) {
            if (conductor.getEstadoConductor() == EstadoConductor.DISPONIBLE ) {
                disponibles.add(conductor);
            }
        }
        return disponibles;
    }
    // =========================
    // REGISTRO DE VEHICULOS
    // =========================
    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException(
                "El vehículo no puede ser null"
            );
        }
        String patente = vehiculo.getPatente();
        if (vehiculos.containsKey(patente)) {
            throw new IllegalArgumentException(
                "Ya existe un vehículo con esa patente"
            );
        }
        vehiculos.put(patente, vehiculo);
    }
    // =========================
    // AGREGAR SERVICIO
    // =========================
    public void agregarServicio(Servicio servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException(
                "El servicio no puede ser null"
            );
        }
        servicios.add(servicio);
    }
    // =========================
    // OBTENER REGISTRO DE VIAJES
    // =========================
    public List<RegistroViaje> obtenerRegistroViaje() {
        List<RegistroViaje> registros = new ArrayList<>();
        for (Viaje viaje : viajes.values()) {
            registros.addAll(viaje.getRegistroViaje());
        }
        return registros;
    }
    // =========================
    // OBTENER USUARIOS
    // =========================
    public List<Usuario> obtenerUsuarios() {
        return new ArrayList<>(usuarios.values());
    }
}