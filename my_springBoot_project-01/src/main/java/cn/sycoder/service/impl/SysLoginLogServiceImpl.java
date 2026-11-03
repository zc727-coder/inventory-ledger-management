package cn.sycoder.service.impl;

import cn.sycoder.domain.SysLoginLog;
import cn.sycoder.mapper.SysLoginLogMapper;
import cn.sycoder.service.ISysLoginLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户登录行为日志模板表 服务实现类
 * </p>
 *
 * @author sy
 * @since 2026-04-13
 */
@Service
public class SysLoginLogServiceImpl extends MPJBaseServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

}
