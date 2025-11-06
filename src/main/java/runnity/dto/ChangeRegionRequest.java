package runnity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRegionRequest {

  private String address;
  private Double lat;
  private Double lng;

}
