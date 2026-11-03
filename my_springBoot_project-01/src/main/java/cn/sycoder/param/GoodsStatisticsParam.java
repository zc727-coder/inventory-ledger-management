package cn.sycoder.param;

import lombok.Getter;
import lombok.Setter;

/**
 * 库存统计查询参数
 */
@Getter
@Setter
public class GoodsStatisticsParam {

    /**
     * 统计范围：month按月，year按年
     */
    private String scope;

    /**
     * 统计年份
     */
    private Integer year;

    /**
     * 统计月份，scope=month时使用
     */
    private Integer month;
}
