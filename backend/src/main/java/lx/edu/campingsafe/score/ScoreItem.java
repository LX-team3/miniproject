package lx.edu.campingsafe.score;

/** 시설 종류 하나에 대한 점수 근거 (가장 가까운 시설 + 거리 + 점수) */
public class ScoreItem {
    private String type;        // police, er, fire, store, toilet
    private int weight;         // 이 항목 최대 점수
    private int fullDistM;      // 이 거리 안이면 만점
    private int zeroDistM;      // 이 거리 밖이면 0점
    private String facilityId;  // 가장 가까운 시설 (없으면 null)
    private String name;
    private Double lat;
    private Double lng;
    private Double distM;       // 거리(m)
    private double point;       // 받은 점수

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public int getFullDistM() { return fullDistM; }
    public void setFullDistM(int fullDistM) { this.fullDistM = fullDistM; }
    public int getZeroDistM() { return zeroDistM; }
    public void setZeroDistM(int zeroDistM) { this.zeroDistM = zeroDistM; }
    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getDistM() { return distM; }
    public void setDistM(Double distM) { this.distM = distM; }
    public double getPoint() { return point; }
    public void setPoint(double point) { this.point = point; }
}
