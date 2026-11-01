package md.modelo;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class Viaje {
    private UUID id;
    private Usuario cliente;
    private Usuario conductor;
    private Servicio servicio;
    private Ubicacion origen;
    private Ubicacion destino;
    private EstadoViaje estado;
    private LocalDateTime fechaHoraSolicitud;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private double costoCalculado;
    private CalificacionViaje calificacionConductor;
    private CalificacionViaje calificacionCliente;
    private String motivoCancelacion;
    private RolUsuario rolCancela;
    private List<RegistroViaje> registros;

    public Viaje(Usuario cliente, Servicio servicio, Ubicacion origen, Ubicacion destino, List<RegistroViaje> registros) {
        this.id = UUID.randomUUID();
        this.cliente = cliente;
        this.servicio = servicio;
        this.origen = origen;
        this.destino = destino;
        this.estado = EstadoViaje.SOLICITADO;
        this.calificacionConductor = CalificacionViaje.NO_CALIFICADO;
        this.calificacionCliente = CalificacionViaje.NO_CALIFICADO;
        this.costoCalculado = calcularCostoEstimado();
        this.registros = new ArrayList<>();
    }

    // Obtiene la distancia y el tiempo estimado desde las entidades Ubicacion
    // invoca al metodo calcularCosto de la clase Servicio.
   
    public double calcularCostoEstimado() {
        if (origen != null && destino != null && servicio != null) {
            double km = origen.calcularDistancia(destino);
            double minutos = km * 2.0; 
            return servicio.calcularCosto(km, minutos);
        }
        return 0.0;
    }

    public void solicitar(LocalDateTime fechaHora) {
        this.fechaHoraSolicitud = fechaHora;
        this.estado = EstadoViaje.SOLICITADO;
    }

    public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
        this.conductor = conductor;
        this.estado = EstadoViaje.ACEPTADO;
        if (conductor != null && conductor.getConductor() != null) {
            conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
        }
    }

    public void iniciar(LocalDateTime fechaHora) {
        this.fechaHoraInicio = fechaHora;
        this.estado = EstadoViaje.INICIADO;
        if (conductor != null && conductor.getConductor() != null) {
            conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
        }
    }

    public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionConductor, CalificacionViaje calificacionCliente) {
        this.fechaHoraFin = fechaHora;
        this.calificacionConductor = calificacionConductor;
        this.calificacionCliente = calificacionCliente;
        this.estado = EstadoViaje.FINALIZADO;
        
        if (conductor != null && conductor.getConductor() != null) {
            conductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
        }
    }

    public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
        this.fechaHoraFin = fechaHora;
        this.motivoCancelacion = motivo;
        this.estado = EstadoViaje.CANCELADO;
        if (usuario != null) {
            this.rolCancela = usuario.getRolActivo();
        }

        if (conductor != null && conductor.getConductor() != null) {
            conductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
        }
    }

    public void rechazar(LocalDateTime fechaHora) {
        this.fechaHoraFin = fechaHora;
        this.estado = EstadoViaje.RECHAZADO;
    }

    public EstadoViaje estadoActual() {
        return this.estado;
    }
    
    public void agregarRegistroViaje(RegistroViaje registro) {
        if (registro == null) {
            throw new IllegalArgumentException(
                "El registro de viaje no puede ser null"
            );
        }

        registros.add(registro);
    }
    // --- Getters y Setters ---

    
	public void setRegistros(List<RegistroViaje> registros) {
		this.registros = registros;
	}

	public UUID getId() {  return id; }
    public Usuario getCliente() { return cliente; }
    public Usuario getConductor() { return conductor;  }
    public Servicio getServicio() { return servicio;  }
    public Ubicacion getOrigen() { return origen; }
    public Ubicacion getDestino() { return destino;  }
    public EstadoViaje getEstadoActual() { return estado;  }
    public LocalDateTime getFechaHoraSolicitud() { return fechaHoraSolicitud;  }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio;   }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin;  }
    public double getCostoCalculado() { return costoCalculado;    }
    public CalificacionViaje getCalificacionConductor() { return calificacionConductor;  }
    public CalificacionViaje getCalificacionCliente() { return calificacionCliente; }
    public String getMotivoCancelacion() { return motivoCancelacion;   }
    public RolUsuario getRolCancela() {  return rolCancela; }
    public void setRolCancela(RolUsuario rolCancela) { this.rolCancela = rolCancela; }
    public List<RegistroViaje> getRegistroViaje() {return registros; }

}