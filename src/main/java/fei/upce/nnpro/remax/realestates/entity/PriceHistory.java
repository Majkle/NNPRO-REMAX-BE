package fei.upce.nnpro.remax.realestates.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp;

    @Column(name = "price")
    private double price;
}