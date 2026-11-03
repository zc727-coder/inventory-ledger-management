package cn.sycoder.service.impl;

import cn.sycoder.domain.Product;
import cn.sycoder.mapper.ProductMapper;
import cn.sycoder.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存产品表 服务实现类
 * </p>
 *
 * @author sy
 * @since 2026-04-10
 */
@Service
public class ProductServiceImpl extends MPJBaseServiceImpl<ProductMapper, Product> implements IProductService {

}
