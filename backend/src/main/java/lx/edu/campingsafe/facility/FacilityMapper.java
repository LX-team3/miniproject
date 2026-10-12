package lx.edu.campingsafe.facility;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FacilityMapper {
    List<Facility> findAll(@Param("types") List<String> types);
    Facility findById(@Param("id") String id);
    List<TypeCount> countByType();
}
