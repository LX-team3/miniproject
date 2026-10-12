package lx.edu.campingsafe.level;

/** 질문의 선택지 하나 */
public class LevelOption {
    private String id;
    private String label;
    private int point;          // 이 선택지를 고르면 받는 점수
    private String value;       // 점수 외 값 (동행 구분 등)
    private boolean exclusive;  // true면 다른 선택지와 같이 못 고름 (해당 없음, 상관없어요)

    public LevelOption(String id, String label, int point) {
        this(id, label, point, null, false);
    }

    public LevelOption(String id, String label, int point, String value, boolean exclusive) {
        this.id = id;
        this.label = label;
        this.point = point;
        this.value = value;
        this.exclusive = exclusive;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public int getPoint() { return point; }
    public String getValue() { return value; }
    public boolean isExclusive() { return exclusive; }
}
