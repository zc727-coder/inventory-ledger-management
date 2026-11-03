package cn.sycoder.mapper;

import cn.sycoder.domain.ProductRecord;
import cn.sycoder.response.ChartItemResponse;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 库存出入库流水台账表 Mapper 接口
 * </p>
 *
 * @author sy
 * @since 2026-04-10
 */
public interface ProductRecordMapper extends MPJBaseMapper<ProductRecord> {

    Integer tableExists(@Param("tableName") String tableName);

    void createMonthlyTable(@Param("tableName") String tableName);

    int insertMonthly(@Param("tableName") String tableName, @Param("record") ProductRecord record);

    List<ProductRecord> selectMonthlyRecords(@Param("tableNames") List<String> tableNames,
                                             @Param("productName") String productName,
                                             @Param("productCode") String productCode,
                                             @Param("location") String location,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    List<ChartItemResponse> selectTopProducts(@Param("tableNames") List<String> tableNames,
                                              @Param("bizType") String bizType,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("limit") int limit);

    List<ChartItemResponse> selectQuantityByPeriod(@Param("tableNames") List<String> tableNames,
                                                   @Param("bizType") String bizType,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("groupByMonth") boolean groupByMonth);
}
