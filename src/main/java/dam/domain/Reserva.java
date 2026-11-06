package dam.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;
    @Column(name = "numero_plazas")
    private int numeroPlazas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public Reserva() {
    }

    public Reserva(Cliente cliente, Actividad actividad, int numeroPlazas, LocalDateTime fechaReserva) {
        this.cliente = cliente;
        this.actividad = actividad;
        this.numeroPlazas = numeroPlazas;
        this.fechaReserva = fechaReserva;
    }

    public Long getId() {
        return id;
    }

    public int getNumeroPlazas() {
        return numeroPlazas;
    }

    public Actividad getActividad() {
        return actividad;
    }

}