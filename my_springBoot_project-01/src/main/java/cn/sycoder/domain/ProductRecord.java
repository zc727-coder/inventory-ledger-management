package cn.sycoder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 库存出入库流水台账表
 * </p>
 *
 * @author sy
 * @since 2026-04-10
 */
@Getter
@Setter
  @TableName("product_record")
public class ProductRecord implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 流水ID
     */
        @TableId(value = "record_id", type = IdType.AUTO)
      private Long recordId;

      /**
     * 关联产品ID
     */
      private Long productId;

      /**
     * 业务日期
     */
      private LocalDateTime recordDate;

      /**
     * 库房名称
     */
      private String warehouseName;

      /**
     * 位置
     */
      private String locationName;

      /**
     * 物件编号
     */
      private String productCode;

      /**
     * 产品名称
     */
      private String productName;

      /**
     * 型号
     */
      private String productType;

      /**
     * 业务类型：CHECK清点 IN入库 OUT出库
     */
      private String bizType;

      /**
     * 出库用途/业务说明
     */
      private String bizReason;

      /**
     * 入库数量
     */
      private Integer inQuantity;

      /**
     * 出库数量
     */
      private Integer outQuantity;

      /**
     * 操作后余量
     */
      private Integer balanceQuantity;

      /**
     * 单位
     */
      private String unit;

      /**
     * 经办人
     */
      private String operatorName;

      /**
     * 备注
     */
      private String remark;

      /**
     * 来源类型：IMPORT导入 MANUAL手工
     */
      private String sourceType;

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
