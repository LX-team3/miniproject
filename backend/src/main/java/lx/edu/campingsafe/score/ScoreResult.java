package lx.edu.campingsafe.score;

import java.util.List;

/** 한 위치의 안심 점수 결과 */
public class ScoreResult {
    private double lat;
    private double lng;
    private double total;        // 0~100
    private String grade;        // 안심 / 보통 / 주의
    private List<ScoreItem> items;

    public ScoreResult(double lat, double lng, double total, String grade, List<ScoreItem> items) {
        this.lat = lat;
        this.lng = lng;
        this.total = total;
        this.grade = grade;
        this.items = items;
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getTotal() { return total; }
    public String getGrade() { return grade; }
    public List<ScoreItem> getItems() { return items; }
}
