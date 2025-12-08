package fei.upce.nnpro.remax.images.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;

}