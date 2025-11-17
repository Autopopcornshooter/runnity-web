package runnity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aot.generate.GeneratedTypeReference;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long regionId;

  @Column(name = "address")
  private String address;
  @Column(name = "lat")
  private double lat;
  @Column(name = "lng")
  private double lng;
}
