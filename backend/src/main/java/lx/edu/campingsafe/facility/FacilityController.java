package lx.edu.campingsafe.facility;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {
    private final FacilityService service;

    public FacilityController(FacilityService service) {
        this.service = service;
    }

    /** GET /api/facilities  또는  /api/facilities?type=police&type=fire */
    @GetMapping
    public List<Facility> list(@RequestParam(name = "type", required = false) List<String> types) {
        return service.getFacilities(types);
    }

    /** GET /api/facilities/types — 종류별 개수 (지도 범례·필터용) */
    @GetMapping("/types")
    public List<TypeCount> types() {
        return service.getTypeCounts();
    }

    /** GET /api/facilities/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Facility> one(@PathVariable("id") String id) {
        Facility f = service.getFacility(id);
        return f == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(f);
    }
}
