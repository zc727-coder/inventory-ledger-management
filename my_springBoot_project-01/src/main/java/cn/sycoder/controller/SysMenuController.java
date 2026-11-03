package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysModule;
import cn.sycoder.domain.SysRoleModule;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.MenuNavResponse;
import cn.sycoder.service.ISysModuleService;
import cn.sycoder.service.ISysRoleModuleService;
import cn.sycoder.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Api(tags = "导航菜单接口")
@RestController
@RequestMapping("/api")
public class SysMenuController {

    @Autowired
    private ISysModuleService sysModuleService;

    @Autowired
    private ISysRoleModuleService sysRoleModuleService;

    @Autowired
    private ISysUserService sysUserService;

    @GetMapping("/menu")
    @ApiOperation("获取当前登录用户可见菜单")
    public Resp<List<MenuNavResponse>> currentUserMenu() {
        SysUser currentLoginUser = UserContext.get();
        if (currentLoginUser == null || currentLoginUser.getUserId() == null) {
            throw new BusinessException("未获取到当前登录用户");
        }

        SysUser currentUser = sysUserService.getById(currentLoginUser.getUserId());
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }
        if (currentUser.getRoleId() == null) {
            throw new BusinessException("当前用户未配置角色");
        }

        LambdaQueryWrapper<SysRoleModule> roleModuleWrapper = new LambdaQueryWrapper<>();
        roleModuleWrapper.eq(SysRoleModule::getRoleId, currentUser.getRoleId())
                .orderByAsc(SysRoleModule::getModuleId);
        List<SysRoleModule> roleModules = sysRoleModuleService.list(roleModuleWrapper);
        if (roleModules == null || roleModules.isEmpty()) {
            return Resp.success("查询成功", new ArrayList<>(), 0L);
        }

        LambdaQueryWrapper<SysModule> moduleWrapper = new LambdaQueryWrapper<>();
        moduleWrapper.orderByAsc(SysModule::getModuleOrder)
                .orderByAsc(SysModule::getModuleId);
        List<SysModule> allModules = sysModuleService.list(moduleWrapper);
        if (allModules == null || allModules.isEmpty()) {
            return Resp.success("查询成功", new ArrayList<>(), 0L);
        }

        Map<Long, SysModule> moduleMap = allModules.stream()
                .collect(Collectors.toMap(SysModule::getModuleId, Function.identity(), (a, b) -> a));

        Set<Long> visibleIds = new LinkedHashSet<>();
        roleModules.forEach(item -> {
            Long moduleId = item.getModuleId();
            while (moduleId != null && moduleId > 0 && !visibleIds.contains(moduleId)) {
                visibleIds.add(moduleId);
                SysModule module = moduleMap.get(moduleId);
                if (module == null || module.getParentId() == null || module.getParentId() <= 0) {
                    break;
                }
                moduleId = module.getParentId();
            }
        });

        List<MenuNavResponse> result = allModules.stream()
                .filter(module -> visibleIds.contains(module.getModuleId()))
                .sorted(Comparator
                        .comparing((SysModule m) -> m.getModuleOrder() == null ? Integer.MAX_VALUE : m.getModuleOrder())
                        .thenComparing(SysModule::getModuleId))
                .map(module -> {
                    MenuNavResponse response = new MenuNavResponse();
                    response.setModuleId(module.getModuleId());
                    response.setParentId(module.getParentId());
                    response.setModuleName(module.getModuleName());
                    response.setModuleUrl(module.getModuleUrl());
                    response.setModuleIcon(module.getModuleIcon());
                    response.setModuleOrder(module.getModuleOrder());
                    response.setModuleLevel(module.getModuleLevel());
                    return response;
                })
                .collect(Collectors.toList());

        return Resp.success("查询成功", result, (long) result.size());
    }
}
