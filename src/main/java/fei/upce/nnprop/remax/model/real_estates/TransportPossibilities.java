package fei.upce.nnprop.remax.model.real_estates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transport_possibilities")
public class TransportPossibilities {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "road", nullable = false)
    private boolean road;

    @Column(name = "highway", nullable = false)
    private boolean highway;

    @Column(name = "train", nullable = false)
    private boolean train;

    @Column(name = "bus", nullable = false)
    private boolean bus;

    @Column(name = "public_transport", nullable = false)
    private boolean publicTransport;

    @Column(name = "airplane", nullable = false)
    private boolean airplane;

    @Column(name = "boat", nullable = false)
    private boolean boat;

    @Column(name = "ferry", nullable = false)
    private boolean ferry;
}