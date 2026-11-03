package cn.sycoder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 库存产品表
 * </p>
 *
 * @author sy
 * @since 2026-04-10
 */
@Getter
@Setter
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 产品ID
     */
        @TableId(value = "product_id", type = IdType.AUTO)
      private Long productId;

      /**
     * 产品名称
     */
      private String productName;

      /**
     * 历史入库总量
     */
      private Integer inventorySum;

      /**
     * 当前库存值
     */
      private Integer inventory;

      /**
     * 历史出库总量
     */
      private Integer inventoryRemove;

      /**
     * 单位
     */
      private String unit;

      /**
     * 库房地址，多个层级用逗号分隔，如：库房,位置,位置编号
     */
      private String location;

      /**
     * 是否告警：1告警，0正常
     */
      private Integer alarm;

      /**
     * 物件编号
     */
      private String productCode;

      /**
     * 告警值，库存低于该值时提醒
     */
      private Integer alarmValue;

      /**
     * 型号
     */
      private String productType;

      /**
     * 产品图片地址
     */
      private String img;

      /**
     * 创建人
     */
      private Long createBy;

      /**
     * 更新人
     */
      private Long updateBy;

      /**
     * 创建时间
     */
      private LocalDateTime createTime;

      /**
     * 更新时间
     */
      private LocalDateTime updateTime;

      /**
     * 逻辑删除：0未删除，1已删除
     */
      @TableLogic
      private Integer deleted;


}
