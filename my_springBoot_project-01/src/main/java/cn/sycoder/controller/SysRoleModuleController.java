package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysModule;
import cn.sycoder.domain.SysRoleModule;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.ModuleResponse;
import cn.sycoder.service.ISysModuleService;
import cn.sycoder.service.ISysRoleModuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "角色菜单权限接口")
@RestController
@RequestMapping("/api")
public class SysRoleModuleController {

    @Autowired
    private ISysModuleService sysModuleService;

    @Autowired
    private ISysRoleModuleService sysRoleModuleService;

    @GetMapping("/RoleRight/tree/{roleId}")
    @ApiOperation("获取角色菜单权限树")
    public Resp<List<ModuleResponse>> roleRightTree(@PathVariable Long roleId) {
        MPJLambdaWrapper<SysModule> wrapper = new MPJLambdaWrapper<SysModule>()
                .selectAll(SysModule.class)
                .select("t1.user_name as addUser")
                .select("t2.user_name as editUser")
                .select("t.module_id as id")
                .select("t.parent_id as pId")
                .select("t.module_name as name")
                .leftJoin(SysUser.class, "t1", SysUser::getUserId, SysModule::getCreateBy)
                .leftJoin(SysUser.class, "t2", SysUser::getUserId, SysModule::getUpdateBy)
                .orderByAsc(SysModule::getModuleOrder);

        List<ModuleResponse> allModules = sysModuleService.selectJoinList(ModuleResponse.class, wrapper);

        allModules.forEach(m -> {
            if (m.getOpen() == null) {
                m.setOpen(true);
            }
            if (m.getChecked() == null) {
                m.setChecked(false);
            }
        });

        LambdaQueryWrapper<SysRoleModule> roleModuleWrapper = new LambdaQueryWrapper<>();
        roleModuleWrapper.eq(SysRoleModule::getRoleId, roleId);
        List<SysRoleModule> roleModules = sysRoleModuleService.list(roleModuleWrapper);
        List<Long> roleModuleIds = roleModules.stream()
                .map(SysRoleModule::getModuleId)
                .collect(Collectors.toList());

        allModules.forEach(m -> m.setChecked(roleModuleIds.contains(m.getId())));

        return Resp.success("查询成功", allModules, (long) allModules.size());
    }

    @PostMapping("/RoleRight/save")
    @ApiOperation("保存角色菜单权限")
    public Resp<Object> roleRightSave(@RequestParam Long roleId, @RequestParam String moduleIds) {
        if (roleId == null) {
            throw new BusinessException("角色ID不能为空");
        }

        LambdaQueryWrapper<SysRoleModule> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysRoleModule::getRoleId, roleId);
        sysRoleModuleService.remove(deleteWrapper);

        if (moduleIds != null && !moduleIds.trim().isEmpty()) {
            String trimmed = moduleIds.trim();
            if (trimmed.startsWith("[")) {
                trimmed = trimmed.substring(1);
            }
            if (trimmed.endsWith("]")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }

            if (!trimmed.isEmpty()) {
                String[] ids = trimmed.split(",");
                SysUser currentUser = UserContext.get();
                Long currentUserId = currentUser != null ? currentUser.getUserId() : null;

                try {
                    for (String id : ids) {
                        String idStr = id.trim();
                        if (!idStr.isEmpty()) {
                            SysRoleModule roleModule = new SysRoleModule();
                            roleModule.setRoleId(roleId);
                            roleModule.setModuleId(Long.parseLong(idStr));
                            roleModule.setCreateBy(currentUserId);
                            sysRoleModuleService.save(roleModule);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new BusinessException("菜单ID格式错误");
                }
            }
        }

        return Resp.success("保存成功", null);
    }
}
