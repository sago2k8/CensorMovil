package co.campeoncloudcomputing.santomas.entidades;

public class Usuario {
	private String cedula;
	private String clave;
	private String idConductor;
	private String idTransportador;
	private String nombres;
	private boolean activo;
	private boolean corriendoServicio = false;
	
	
	public String getCedula() {
		return cedula;
	}
	public void setCedula(String cedula) {
		this.cedula = cedula;
	}
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getIdConductor() {
		return idConductor;
	}
	public void setIdConductor(String idConductor) {
		this.idConductor = idConductor;
	}
	public String getIdTransportador() {
		return idTransportador;
	}
	public void setIdTransportador(String idTransportador) {
		this.idTransportador = idTransportador;
	}
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	public boolean isCorriendoServicio() {
		return corriendoServicio;
	}
	public void setCorriendoServicio(boolean corriendoServicio) {
		this.corriendoServicio = corriendoServicio;
	}
}
