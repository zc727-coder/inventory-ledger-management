package cn.sycoder.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 库存统计图表响应
 */
@Getter
@Setter
public class GoodsStatisticsResponse {

    private String scope;

    private String startDate;

    private String endDate;

    private List<ChartItemResponse> loginUserTop5;

    private List<ChartItemResponse> stockOutProductTop5;

    private List<ChartItemResponse> stockInQuantity;

    private List<ChartItemResponse> stockOutQuantity;
}
