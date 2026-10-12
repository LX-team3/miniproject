package lx.edu.campingsafe.facility;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FacilityService {
    private final FacilityMapper mapper;

    public FacilityService(FacilityMapper mapper) {
        this.mapper = mapper;
    }

    public List<Facility> getFacilities(List<String> types) {
        return mapper.findAll(types);
    }

    public Facility getFacility(String id) {
        return mapper.findById(id);
    }

    public List<TypeCount> getTypeCounts() {
        return mapper.countByType();
    }
}
