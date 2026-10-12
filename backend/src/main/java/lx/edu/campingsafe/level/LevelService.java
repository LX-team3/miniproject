package lx.edu.campingsafe.level;

import java.util.*;
import lx.edu.campingsafe.score.ScoreService;
import org.springframework.stereotype.Service;

/**
 * 캠핑 수준 진단 — DB 없이 질문과 가중치를 코드에 직접 정의
 * 가중치를 바꾸려면 아래 QUESTIONS 의 점수만 고치면 됨
 */
@Service
public class LevelService {
    // 수준 구간 — 발표 전에 팀에서 정한 값으로 바꾸세요
    public static final int EXPERT_MIN = 70;  // 70점 이상: 숙련
    public static final int MIDDLE_MIN = 40;  // 40~69: 중급, 그 아래: 입문

    // ------------------------------------------------------------------
    // 질문지 (점수 문항 5개 = 100점, 참고 문항 2개)
    // ------------------------------------------------------------------
    private static final List<LevelQuestion> QUESTIONS = List.of(
        new LevelQuestion("count", "캠핑을 몇 번 해 보셨나요?", "1박 이상 머문 캠핑만 세어 주세요.",
            "single", true, 25, List.of(
                new LevelOption("count_0",   "아직 없어요", 0),
                new LevelOption("count_1_4", "1~4회",       8),
                new LevelOption("count_5_9", "5~9회",      16),
                new LevelOption("count_10",  "10회 이상",  25))),

        new LevelQuestion("style", "주로 어떤 캠핑을 해 보셨나요?", "가장 많이 해 본 형태 하나를 골라 주세요.",
            "single", true, 20, List.of(
                new LevelOption("style_none",  "해 본 적 없어요",      0),
                new LevelOption("style_glamp", "글램핑 · 카라반",      4),
                new LevelOption("style_auto",  "관리형 오토캠핑장",   10),
                new LevelOption("style_wild",  "차박 · 노지 캠핑",    16),
                new LevelOption("style_back",  "백패킹 · 오지 캠핑",  20))),

        new LevelQuestion("tent", "텐트(타프) 설치는 어느 정도 할 수 있나요?", null,
            "single", true, 15, List.of(
                new LevelOption("tent_none",  "해 본 적 없어요",               0),
                new LevelOption("tent_help",  "도움을 받으면 칠 수 있어요",    5),
                new LevelOption("tent_alone", "혼자서 칠 수 있어요",          10),
                new LevelOption("tent_storm", "바람 · 비 속에서도 혼자 가능", 15))),

        new LevelQuestion("safety", "알고 있거나 평소에 하는 것을 모두 골라 주세요",
            "해당하는 게 없으면 '해당 없음'을 골라 주세요.",
            "multi", true, 20, List.of(
                new LevelOption("safety_aid",  "기본 응급처치 (지혈 · 화상)",           5),
                new LevelOption("safety_ext",  "소화기 사용법",                         5),
                new LevelOption("safety_co",   "텐트 안 난로 · 숯의 일산화탄소 위험",   5),
                new LevelOption("safety_wx",   "출발 전 기상특보 · 대피로 확인",        5),
                new LevelOption("safety_none", "해당 없음", 0, null, true))),

        new LevelQuestion("season", "가장 힘든 조건에서 해 본 캠핑은?", null,
            "single", true, 20, List.of(
                new LevelOption("season_none",   "1박은 해 본 적 없어요",  0),
                new LevelOption("season_summer", "여름 1박",               6),
                new LevelOption("season_cool",   "봄 · 가을 쌀쌀한 밤",   12),
                new LevelOption("season_winter", "영하의 겨울 캠핑",      20))),

        new LevelQuestion("region", "선호하는 캠핑 지역은 어디인가요?", "여러 곳을 골라도 돼요. (점수에는 들어가지 않아요)",
            "multi", false, 0, List.of(
                new LevelOption("region_capital", "서울 · 경기 · 인천", 0),
                new LevelOption("region_gw",      "강원", 0),
                new LevelOption("region_cc",      "충청", 0),
                new LevelOption("region_jl",      "전라", 0),
                new LevelOption("region_gs",      "경상", 0),
                new LevelOption("region_jj",      "제주", 0),
                new LevelOption("region_any",     "상관없어요", 0, null, true))),

        new LevelQuestion("companion", "주로 누구와 함께 가나요?", "아이와 함께라면 더 안전한 캠핑장을 권해 드려요.",
            "single", false, 0, List.of(
                new LevelOption("comp_solo",    "혼자",             0, "solo",    false),
                new LevelOption("comp_friends", "연인 · 친구",      0, "friends", false),
                new LevelOption("comp_kids",    "가족 (아이 동반)", 0, "kids",    false),
                new LevelOption("comp_pet",     "반려동물",         0, "pet",     false)))
    );

    public List<LevelQuestion> getQuestions() {
        return QUESTIONS;
    }

    /** 답변 채점 → 점수·수준 */
    public LevelResult evaluate(Map<String, List<String>> answers) {
        if (answers == null) throw new IllegalArgumentException("답변이 없습니다.");

        int got = 0, max = 0;
        boolean withKids = false;
        List<LevelItem> items = new ArrayList<>();
        List<String> regionLabels = new ArrayList<>();

        for (LevelQuestion q : QUESTIONS) {
            List<LevelOption> chosen = validate(q, answers.get(q.getId()));

            if (q.isScored()) {
                int sum = 0;
                for (LevelOption o : chosen) sum += o.getPoint();
                int point = Math.min(sum, q.getMaxPoint());   // multi 문항은 최대 점수에서 자름
                got += point;
                max += q.getMaxPoint();
                items.add(new LevelItem(q.getId(), q.getTitle(), labels(chosen), point, q.getMaxPoint()));
            }
            for (LevelOption o : chosen) {
                if ("kids".equals(o.getValue())) withKids = true;
                if ("region".equals(q.getId()) && !o.isExclusive()) regionLabels.add(o.getLabel());
            }
        }

        int total = max == 0 ? 0 : (int) Math.round(got * 100.0 / max);   // 100점 만점으로 환산
        String level = level(total);

        LevelResult r = new LevelResult();
        r.setTotalScore(total);
        r.setLevel(level);
        r.setDescription(description(level));
        r.setWithKids(withKids);
        r.setMinSafeScore(minSafeScore(level, withKids));
        r.setRegionLabels(regionLabels);
        r.setItems(items);
        return r;
    }

    // ------------------------------------------------------------------

    /** 질문 하나의 답이 올바른지 확인하고 선택지 객체로 바꿈 */
    private List<LevelOption> validate(LevelQuestion q, List<String> picked) {
        if (picked == null || picked.isEmpty())
            throw new IllegalArgumentException("'" + q.getTitle() + "' 에 답해 주세요.");
        if ("single".equals(q.getInputType()) && picked.size() != 1)
            throw new IllegalArgumentException("'" + q.getTitle() + "' 은 하나만 고를 수 있어요.");

        List<LevelOption> chosen = new ArrayList<>();
        for (String id : new LinkedHashSet<>(picked)) {
            LevelOption found = null;
            for (LevelOption o : q.getOptions()) if (o.getId().equals(id)) found = o;
            if (found == null) throw new IllegalArgumentException("잘못된 선택지입니다: " + id);
            chosen.add(found);
        }
        if (chosen.size() > 1 && chosen.stream().anyMatch(LevelOption::isExclusive))
            throw new IllegalArgumentException("'" + q.getTitle() + "' 에서 '해당 없음/상관없어요'는 다른 항목과 같이 고를 수 없어요.");
        return chosen;
    }

    public static String level(int total) {
        if (total >= EXPERT_MIN) return "숙련";
        if (total >= MIDDLE_MIN) return "중급";
        return "입문";
    }

    private static String description(String level) {
        switch (level) {
            case "숙련": return "어떤 환경에서도 스스로 대처할 수 있어요. 노지·오지 캠핑장도 도전해 보세요.";
            case "중급": return "기본기는 충분해요. 안심 점수 '보통' 이상인 캠핑장이 잘 맞아요.";
            default:     return "처음이라면 주변에 경찰서·응급실·편의점이 가까운 '안심' 등급 캠핑장부터 시작해 보세요.";
        }
    }

    /**
     * 수준별 권장 최소 안심 점수 (기존 ScoreService 의 등급 기준 상수만 사용, DB 조회 없음)
     *   입문 → 안심 이상, 중급 → 보통 이상, 숙련 → 제한 없음 / 아이 동반이면 한 단계 더 엄격하게
     */
    private static double minSafeScore(String level, boolean withKids) {
        int step = "입문".equals(level) ? 2 : "중급".equals(level) ? 1 : 0;
        if (withKids) step = Math.min(2, step + 1);
        return step == 2 ? ScoreService.SAFE_MIN : step == 1 ? ScoreService.NORMAL_MIN : 0;
    }

    private static String labels(List<LevelOption> chosen) {
        StringJoiner j = new StringJoiner(" · ");
        for (LevelOption o : chosen) j.add(o.getLabel());
        return j.toString();
    }
}
