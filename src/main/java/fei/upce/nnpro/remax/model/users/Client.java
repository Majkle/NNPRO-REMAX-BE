package fei.upce.nnpro.remax.model.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("CLIENT")
public class Client extends RemaxUser {
}