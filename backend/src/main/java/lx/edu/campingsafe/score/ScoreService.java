package lx.edu.campingsafe.score;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScoreService {
    // 등급 기준 — 발표 전에 팀에서 정한 값으로 바꾸세요
    public static final double SAFE_MIN = 70;   // 70점 이상: 안심
    public static final double NORMAL_MIN = 40; // 40~70: 보통, 그 아래: 주의

    private final ScoreMapper mapper;

    public ScoreService(ScoreMapper mapper) {
        this.mapper = mapper;
    }

    public static String grade(double total) {
        if (total >= SAFE_MIN) return "안심";
        if (total >= NORMAL_MIN) return "보통";
        return "주의";
    }

    public ScoreResult scoreAt(double lat, double lng) {
        List<ScoreItem> items = mapper.scoreAt(lat, lng);
        double total = 0;
        for (ScoreItem item : items) total += item.getPoint();
        total = Math.round(total * 10) / 10.0;
        return new ScoreResult(lat, lng, total, grade(total), items);
    }

    public List<CampScore> campScores() {
        List<CampScore> list = mapper.campScores();
        for (CampScore c : list) c.setGrade(grade(c.getTotalScore()));
        return list;
    }
}
