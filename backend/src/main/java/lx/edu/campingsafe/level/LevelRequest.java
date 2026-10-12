package lx.edu.campingsafe.level;

import java.util.List;
import java.util.Map;

/**
 * POST /api/level 요청 본문
 * 예) { "answers": { "count": ["count_5_9"], "safety": ["safety_aid", "safety_ext"], ... } }
 *     key = 질문 id, value = 고른 선택지 id 목록 (single 문항도 배열로 보냄)
 */
public class LevelRequest {
    private Map<String, List<String>> answers;

    public Map<String, List<String>> getAnswers() { return answers; }
    public void setAnswers(Map<String, List<String>> answers) { this.answers = answers; }
}
