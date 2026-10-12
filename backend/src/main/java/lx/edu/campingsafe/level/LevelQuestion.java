package lx.edu.campingsafe.level;

import java.util.List;

/** 질문 하나 + 선택지 목록 */
public class LevelQuestion {
    private String id;
    private String title;
    private String helpText;
    private String inputType;   // single / multi
    private boolean scored;     // false면 점수엔 안 들어가는 참고용 문항
    private int maxPoint;       // 이 문항 최대 점수 (multi는 합계를 여기서 자름)
    private List<LevelOption> options;

    public LevelQuestion(String id, String title, String helpText, String inputType,
                         boolean scored, int maxPoint, List<LevelOption> options) {
        this.id = id;
        this.title = title;
        this.helpText = helpText;
        this.inputType = inputType;
        this.scored = scored;
        this.maxPoint = maxPoint;
        this.options = options;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getHelpText() { return helpText; }
    public String getInputType() { return inputType; }
    public boolean isScored() { return scored; }
    public int getMaxPoint() { return maxPoint; }
    public List<LevelOption> getOptions() { return options; }
}
