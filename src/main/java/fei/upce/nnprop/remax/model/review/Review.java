package fei.upce.nnprop.remax.model.review;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "overall", nullable = false)
    @Max(5)
    @Min(0)
    private int overall;

    @Column(name = "speed", nullable = false)
    @Max(5)
    @Min(0)
    private int speed;

    @Column(name = "communication", nullable = false)
    @Max(5)
    @Min(0)
    private int communication;

    @Column(name = "professionality", nullable = false)
    @Max(5)
    @Min(0)
    private int professionality;

    @Column(name = "fairness", nullable = false)
    @Max(5)
    @Min(0)
    private int fairness;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

}