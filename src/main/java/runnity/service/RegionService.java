package runnity.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import runnity.repository.RegionRepository;

@Service
@AllArgsConstructor
public class RegionService {

  RegionRepository regionRepository;

  
}
