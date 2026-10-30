package md.datos.DTO;

import java.util.LinkedHashSet;
import java.util.Set;

import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class VehiculoDTO {

	    private String patente;
	    private String modelo;
	    private int capacidadPasajeros;
	    private TipoVehiculo tipoVehiculo;
	    private CategoriaVehiculo categoriaVehiculo;
	    private Set<TipoServicio> tiposServicios;

	    public VehiculoDTO(String patente, String modelo, int capacidadPasajeros, TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo) {
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

		public String getPatente() {
			return patente;
		}

		public void setPatente(String patente) {
			this.patente = patente;
		}

		public String getModelo() {
			return modelo;
		}

		public void setModelo(String modelo) {
			this.modelo = modelo;
		}

		public int getCapacidadPasajeros() {
			return capacidadPasajeros;
		}

		public void setCapacidadPasajeros(int capacidadPasajeros) {
			this.capacidadPasajeros = capacidadPasajeros;
		}

		public TipoVehiculo getTipoVehiculo() {
			return tipoVehiculo;
		}

		public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
			this.tipoVehiculo = tipoVehiculo;
		}

		public CategoriaVehiculo getCategoriaVehiculo() {
			return categoriaVehiculo;
		}

		public void setCategoriaVehiculo(CategoriaVehiculo categoriaVehiculo) {
			this.categoriaVehiculo = categoriaVehiculo;
		}

		public Set<TipoServicio> getTiposServicios() {
			return tiposServicios;
		}

		public void setTiposServicios(Set<TipoServicio> tiposServicios) {
			this.tiposServicios = tiposServicios;
		}
}
