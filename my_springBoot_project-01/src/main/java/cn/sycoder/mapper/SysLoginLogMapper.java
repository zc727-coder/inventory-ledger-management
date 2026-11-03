package cn.sycoder.mapper;

import cn.sycoder.domain.SysLoginLog;
import cn.sycoder.response.ChartItemResponse;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户登录行为日志模板表 Mapper 接口
 * </p>
 *
 * @author sy
 * @since 2026-04-13
 */
public interface SysLoginLogMapper extends MPJBaseMapper<SysLoginLog> {

    Integer tableExists(@Param("tableName") String tableName);

    void createMonthlyTable(@Param("tableName") String tableName);

    int insertMonthly(@Param("tableName") String tableName, @Param("log") SysLoginLog log);

    List<ChartItemResponse> selectTopLoginUsers(@Param("tableNames") List<String> tableNames,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("limit") int limit);
}
