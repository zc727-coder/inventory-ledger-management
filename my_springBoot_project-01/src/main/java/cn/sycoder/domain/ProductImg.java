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
 * 产品图片表
 * </p>
 *
 * @author sy
 * @since 2026-04-13
 */
@Getter
@Setter
  @TableName("product_img")
public class ProductImg implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 图片ID
     */
        @TableId(value = "img_id", type = IdType.AUTO)
      private Long imgId;

      /**
     * 产品ID
     */
      private Long productId;

      /**
     * 图片地址
     */
      private String imgUrl;

      /**
     * 是否主图：1是，0否
     */
      private Integer isMain;

      /**
     * 排序值
     */
      private Integer sort;

      /**
     * 创建人
     */
      private Long createBy;

      /**
     * 创建时间
     */
      private LocalDateTime createTime;

      /**
     * 逻辑删除：0未删除，1已删除
     */
      @TableLogic
      private Integer deleted;


}
