package lx.edu.campingsafe.level;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/level")
public class LevelController {
    private final LevelService service;

    public LevelController(LevelService service) {
        this.service = service;
    }

    /** GET /api/level/questions — 질문지 + 선택지 */
    @GetMapping("/questions")
    public List<LevelQuestion> questions() {
        return service.getQuestions();
    }

    /** POST /api/level — 답변 채점 → 점수·수준 (화면에서 fetch로 비동기 호출) */
    @PostMapping
    public ResponseEntity<?> evaluate(@RequestBody LevelRequest req) {
        try {
            return ResponseEntity.ok(service.evaluate(req.getAnswers()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
