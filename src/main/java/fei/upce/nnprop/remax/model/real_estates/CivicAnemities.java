package fei.upce.nnprop.remax.model.real_estates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "civic_anemities")
public class CivicAnemities {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "bus_stop", nullable = false)
    private boolean busStop;

    @Column(name = "train_station", nullable = false)
    private boolean trainStation;

    @Column(name = "post_office", nullable = false)
    private boolean postOffice;

    @Column(name = "atm", nullable = false)
    private boolean atm;

    @Column(name = "general_practitioner", nullable = false)
    private boolean generalPractitioner;

    @Column(name = "veterinarian", nullable = false)
    private boolean veterinarian;

    @Column(name = "elementary_school", nullable = false)
    private boolean elementarySchool;

    @Column(name = "kindergarten", nullable = false)
    private boolean kindergarten;

    @Column(name = "supermarket", nullable = false)
    private boolean supermarket;

    @Column(name = "small_shop", nullable = false)
    private boolean smallShop;

    @Column(name = "restaurant", nullable = false)
    private boolean restaurant;

    @Column(name = "pub", nullable = false)
    private boolean pub;

    @Column(name = "playground", nullable = false)
    private boolean playground;

    @Column(name = "subway", nullable = false)
    private boolean subway;
}