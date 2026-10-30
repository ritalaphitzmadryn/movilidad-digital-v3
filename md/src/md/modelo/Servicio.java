package md.modelo;

public class Servicio {
    private String nombre;
    private TipoServicio tipoServicio;				// PASAJEROS, ENVIOS
    private TipoVehiculo tipoVehiculo;       		// AUTO, MOTO
    private CategoriaVehiculo categoriaVehiculo;	// ESTANDAR, CONFORT, PREMIUM
    private double tarifaBase;
    private double precioKm;
    private double precioMinuto;
    
    public Servicio(String nombre, TipoServicio tipoServicio, TipoVehiculo tipoVehiculo, 
            CategoriaVehiculo categoriaVehiculo, double tarifaBase, 
            double precioKm, double precioMinuto) {
    	this.nombre = nombre;
    	this.tipoServicio = tipoServicio;
    	this.tipoVehiculo = tipoVehiculo;
    	this.categoriaVehiculo = categoriaVehiculo	;
    	this.tarifaBase = tarifaBase;
    	this.precioKm = precioKm;
    	this.precioMinuto = precioMinuto;
}

    public String getNombre() { return nombre; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public TipoVehiculo getTipoVehiculo() { return tipoVehiculo; }
    public CategoriaVehiculo getCategoriaVehiculo() { return categoriaVehiculo; }
    public double getTarifaBase() { return tarifaBase; }
    public double getValorPorKm() { return precioKm; }
    public double getValorPorMinuto() { return precioMinuto; }

    public double calcularCosto(double kilometros, double minutos) {
        return tarifaBase + (kilometros * precioKm) + (minutos * precioMinuto);
    }
}