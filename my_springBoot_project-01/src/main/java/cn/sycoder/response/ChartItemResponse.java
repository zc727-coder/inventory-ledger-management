package cn.sycoder.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 图表通用名称和值
 */
@Getter
@Setter
public class ChartItemResponse {

    private String name;

    private Long value;
}
