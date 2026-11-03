package cn.sycoder.service.impl;

import cn.sycoder.domain.ProductRecord;
import cn.sycoder.mapper.ProductRecordMapper;
import cn.sycoder.service.IProductRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存出入库流水台账表 服务实现类
 * </p>
 *
 * @author sy
 * @since 2026-04-10
 */
@Service
public class ProductRecordServiceImpl extends MPJBaseServiceImpl<ProductRecordMapper, ProductRecord> implements IProductRecordService {

}
