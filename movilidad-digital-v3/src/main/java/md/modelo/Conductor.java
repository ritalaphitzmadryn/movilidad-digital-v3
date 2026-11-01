package md.modelo;

import java.util.ArrayList;
import java.util.List;

public class Conductor {
    private String licenciaConducir;
    private EstadoConductor estadoConductor;
    private List<Vehiculo> vehiculos;
    private Vehiculo vehiculoActivo;
    private List<Viaje> viajes;

    public Conductor(String licenciaConducir, Vehiculo vehiculo) {
        this.licenciaConducir = licenciaConducir;
        this.estadoConductor = EstadoConductor.FUERA_DE_SERVICIO;
        this.vehiculos = new ArrayList<>();
        if (vehiculo != null) {
            this.vehiculos.add(vehiculo);
            this.vehiculoActivo = vehiculo;
        }
        this.viajes = new ArrayList<>();
    }

    public void agregarVehiculo(Vehiculo vehiculo) {
        this.vehiculos.add(vehiculo);
        if (this.vehiculoActivo == null) {
            this.vehiculoActivo = vehiculo;
        }
    }
    public void agregarViaje(Viaje viaje) { this.viajes.add(viaje);  }
    public String getLicenciaConducir() {  return licenciaConducir;  }
    public EstadoConductor getEstadoConductor() { return estadoConductor;   
    }
    public void setEstadoConductor(EstadoConductor estadoConductor) {
        this.estadoConductor = estadoConductor;
    }
    public Vehiculo getVehiculoActivo() { return vehiculoActivo;  }
    public void setVehiculoActivo(Vehiculo vehiculoActivo) {
        this.vehiculoActivo = vehiculoActivo;
    }

    public List<Vehiculo> getVehiculos() { return vehiculos;  }
    public List<Viaje> getViajes() { return viajes;   }
}