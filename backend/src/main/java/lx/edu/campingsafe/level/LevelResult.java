package lx.edu.campingsafe.level;

import java.util.List;

/** 캠핑 수준 진단 결과 (POST /api/level 응답) */
public class LevelResult {
    private int totalScore;              // 0~100
    private String level;                // 입문 / 중급 / 숙련
    private String description;          // 수준 설명 문구
    private double minSafeScore;         // 이 수준에 권장하는 캠핑장 최소 안심 점수
    private List<String> regionLabels;   // 선호 지역 (비어 있으면 전국)
    private boolean withKids;
    private List<LevelItem> items;       // 문항별 점수 근거

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMinSafeScore() { return minSafeScore; }
    public void setMinSafeScore(double minSafeScore) { this.minSafeScore = minSafeScore; }
    public List<String> getRegionLabels() { return regionLabels; }
    public void setRegionLabels(List<String> regionLabels) { this.regionLabels = regionLabels; }
    public boolean isWithKids() { return withKids; }
    public void setWithKids(boolean withKids) { this.withKids = withKids; }
    public List<LevelItem> getItems() { return items; }
    public void setItems(List<LevelItem> items) { this.items = items; }
}
