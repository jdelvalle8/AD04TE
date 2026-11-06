package dam.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "actividad")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "localidad")
    private String localidad;
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;
    @Column(name = "plazas_totales")
    private int plazasTotales;
    @Column(name = "plazas_disponibles")
    private int plazasDisponibles;

    //clave ajena del monitor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id")
    private Monitor monitor;

    public Actividad() {
    }

    public Actividad(String nombre, String localidad, LocalDateTime fechaHora, int plazasTotales) {
        this.nombre = nombre;
        this.localidad = localidad;
        this.fechaHora = fechaHora;
        this.plazasTotales = plazasTotales;
        //al crearla las disponibles son las mismas que las totales
        this.plazasDisponibles = plazasTotales;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLocalidad() {
        return localidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getPlazasTotales() {
        return plazasTotales;
    }

    public int getPlazasDisponibles() {
        return plazasDisponibles;
    }

    public void setPlazasDisponibles(int plazasDisponibles) {
        this.plazasDisponibles = plazasDisponibles;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

}