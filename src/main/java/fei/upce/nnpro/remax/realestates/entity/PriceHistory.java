package fei.upce.nnpro.remax.realestates.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "price_history")
@NoArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp;

    @Column(name = "price")
    private double price;

    @ManyToOne(optional = false)
    @JoinColumn(name = "real_estate_id", nullable = false)
    private RealEstate realEstate;

    public PriceHistory(double price, RealEstate realEstate) {
        this.price = price;
        this.realEstate = realEstate;
        this.timestamp = ZonedDateTime.now();
    }
}