package lx.edu.campingsafe.level;

/** 점수 문항 하나의 결과 (화면의 '점수 근거' 표시용) */
public class LevelItem {
    private String questionId;
    private String title;
    private String answer;   // 고른 선택지 이름 (여러 개면 ' · '로 연결)
    private int point;
    private int maxPoint;

    public LevelItem(String questionId, String title, String answer, int point, int maxPoint) {
        this.questionId = questionId;
        this.title = title;
        this.answer = answer;
        this.point = point;
        this.maxPoint = maxPoint;
    }

    public String getQuestionId() { return questionId; }
    public String getTitle() { return title; }
    public String getAnswer() { return answer; }
    public int getPoint() { return point; }
    public int getMaxPoint() { return maxPoint; }
}
