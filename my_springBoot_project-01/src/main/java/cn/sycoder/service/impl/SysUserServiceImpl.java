package cn.sycoder.service.impl;

import cn.sycoder.domain.SysUser;
import cn.sycoder.mapper.SysUserMapper;
import cn.sycoder.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author sy
 * @since 2026-03-26
 */
@Service
public class SysUserServiceImpl extends MPJBaseServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

}
