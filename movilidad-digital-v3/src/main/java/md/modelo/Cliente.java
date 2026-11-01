package md.modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private List<Viaje> viajes;

    public Cliente() {
        this.viajes = new ArrayList<>();
    }

    public boolean enViaje() {
        for (Viaje v : viajes) {
            if (v.getEstadoActual() == EstadoViaje.INICIADO || v.getEstadoActual() == EstadoViaje.SOLICITADO || v.getEstadoActual() == EstadoViaje.ACEPTADO) {
                return true;
            }
        }
        return false;
    }

    public void agregarViaje(Viaje viaje) {
        this.viajes.add(viaje);
    }

    public List<Viaje> getViajes() {
        return viajes;
    }
}
