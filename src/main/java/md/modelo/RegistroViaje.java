package md.modelo;

import java.time.LocalDateTime;

public class RegistroViaje {
    private LocalDateTime fechaHora;
    private EstadoViaje estadoViaje;

    public RegistroViaje(LocalDateTime fechaHora, EstadoViaje estadoViaje) {
        this.fechaHora = fechaHora;
        this.estadoViaje = estadoViaje;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoViaje getEstadoViaje() {
        return estadoViaje;
    }
}