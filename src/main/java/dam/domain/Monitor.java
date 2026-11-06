package dam.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "monitor")
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "especialidad")
    private String especialidad;

    //la clave ajena esta en Actividad
    @OneToMany(mappedBy = "monitor")
    private List<Actividad> actividades = new ArrayList<>();

    public Monitor() {
    }

    public Monitor(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    //para que los dos lados queden igual
    public void addActividad(Actividad actividad) {
        actividades.add(actividad);
        actividad.setMonitor(this);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

}