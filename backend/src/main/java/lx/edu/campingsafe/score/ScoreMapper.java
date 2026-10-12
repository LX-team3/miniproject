package lx.edu.campingsafe.score;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScoreMapper {
    List<ScoreItem> scoreAt(@Param("lat") double lat, @Param("lng") double lng);
    List<CampScore> campScores();
}
