package md.datos.DTO;

import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class ServicioDTO {
	
    private String nombre;
    private TipoServicio tipoServicio;				// PASAJEROS, ENVIOS
    private TipoVehiculo tipoVehiculo;       		// AUTO, MOTO
    private CategoriaVehiculo categoriaVehiculo;	// ESTANDAR, CONFORT, PREMIUM
    private double tarifaBase;
    private double precioKm;
    private double precioMinuto;

    // Constructor completo para mapear desde la BD o TXT
    public ServicioDTO(String nombre, TipoServicio tipoServicio, TipoVehiculo tipoVehiculo,
    		CategoriaVehiculo categoriaVehiculo, double tarifaBase, double precioKm, double precioMinuto) {
    	
        this.nombre = nombre;
        this.tipoServicio = tipoServicio;
        this.tipoVehiculo = tipoVehiculo;
        this.categoriaVehiculo = categoriaVehiculo;
        this.tarifaBase = tarifaBase;
        this.precioKm = precioKm;
        this.precioMinuto = precioMinuto;
    }

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public TipoServicio getTipoServicio() {
		return tipoServicio;
	}

	public void setTipoServicio(TipoServicio tipoServicio) {
		this.tipoServicio = tipoServicio;
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

	public double getTarifaBase() {
		return tarifaBase;
	}

	public void setTarifaBase(double tarifaBase) {
		this.tarifaBase = tarifaBase;
	}

	public double getPrecioKm() {
		return precioKm;
	}

	public void setPrecioKm(double precioKm) {
		this.precioKm = precioKm;
	}

	public double getPrecioMinuto() {
		return precioMinuto;
	}

	public void setPrecioMinuto(double precioMinuto) {
		this.precioMinuto = precioMinuto;
	}

}
