package md.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Vehiculo {
    private String patente;
    private String modelo;
    private int capacidadPasajeros;
    private TipoVehiculo tipoVehiculo;
    private CategoriaVehiculo categoriaVehiculo;
    private Set<TipoServicio> tiposServicios;

    public Vehiculo(String patente, String modelo, int capacidadPasajeros, TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo) {
        if (patente == null || patente.isBlank()) {
        	throw new IllegalArgumentException("La patente no puede ser nula o vacía.");
        }
    	this.patente = patente;
        this.modelo = modelo;
        this.capacidadPasajeros = capacidadPasajeros;
        this.tipoVehiculo = tipoVehiculo;
        this.categoriaVehiculo = categoriaVehiculo;
        this.tiposServicios = new LinkedHashSet<>();	//LinkedHashSet mantiene el orden de inserción y evita duplicados
    }

    public void agregarTipoServicio(TipoServicio tipoServicio) {
        if (tiposServicios.size() >= 2 && !tiposServicios.contains(tipoServicio)) {
            throw new IllegalStateException("Un vehiculo no puede tener asignados más de 2 tipos de servicio.");
        }
        //La condición previa valida el tamaño. Set ignora automáticamente si el elemento ya existe 
        tiposServicios.add(tipoServicio);
    }

    public String getPatente() {
        return patente;
    }

    public String getModelo() {
        return modelo;
    }

    public int getCapacidadPasajeros() {
        return capacidadPasajeros;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public CategoriaVehiculo getCategoriaVehiculo() {
        return categoriaVehiculo;
    }

    public Set<TipoServicio> getTiposServicios() {
        return Collections.unmodifiableSet(tiposServicios);
    }
}