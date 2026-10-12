package lx.edu.campingsafe.score;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/score")
public class ScoreController {
    private final ScoreService service;

    public ScoreController(ScoreService service) {
        this.service = service;
    }

    /** GET /api/score?lat=38.069&lng=128.17 — 아무 지점의 안심 점수 */
    @GetMapping
    public ResponseEntity<ScoreResult> at(@RequestParam("lat") double lat, @RequestParam("lng") double lng) {
        if (lat < 33 || lat > 39 || lng < 124 || lng > 132) {
            return ResponseEntity.badRequest().build(); // 대한민국 범위 밖
        }
        return ResponseEntity.ok(service.scoreAt(lat, lng));
    }

    /** GET /api/score/camps — 캠핑장 전체 점수 순위 */
    @GetMapping("/camps")
    public List<CampScore> camps() {
        return service.campScores();
    }
}
