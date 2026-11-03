package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysPermission;
import cn.sycoder.domain.SysRole;
import cn.sycoder.domain.SysRolePermission;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.RolePermissionSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.service.ISysPermissionService;
import cn.sycoder.service.ISysRolePermissionService;
import cn.sycoder.service.ISysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "角色权限接口")
@RestController
@RequestMapping("/api")
public class SysRolePermissionController {

    @Autowired
    private ISysRolePermissionService sysRolePermissionService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysPermissionService sysPermissionService;

    @PostMapping("/RolePermission/save")
    @ApiOperation("保存角色权限")
    public Resp<Object> rolePermissionSave(RolePermissionSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getRoleId() == null) {
            throw new BusinessException("角色ID不能为空");
        }

        SysRole role = sysRoleService.getById(param.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        List<Long> permissionIdList = new ArrayList<>();
        if (param.getPermissionIds() != null && !param.getPermissionIds().trim().isEmpty()) {
            try {
                permissionIdList = Arrays.stream(param.getPermissionIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .distinct()
                        .collect(Collectors.toList());
            } catch (NumberFormatException ex) {
                throw new BusinessException("权限ID格式错误");
            }
        }

        if (!permissionIdList.isEmpty()) {
            LambdaQueryWrapper<SysPermission> permissionWrapper = new LambdaQueryWrapper<>();
            permissionWrapper.in(SysPermission::getPermissionId, permissionIdList);
            long permissionCount = sysPermissionService.count(permissionWrapper);
            if (permissionCount != permissionIdList.size()) {
                throw new BusinessException("存在无效的权限ID");
            }
        }

        LambdaQueryWrapper<SysRolePermission> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysRolePermission::getRoleId, param.getRoleId());
        sysRolePermissionService.remove(deleteWrapper);

        if (!permissionIdList.isEmpty()) {
            SysUser currentUser = UserContext.get();
            Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
            LocalDateTime now = LocalDateTime.now();

            List<SysRolePermission> saveList = permissionIdList.stream()
                    .map(permissionId -> {
                        SysRolePermission item = new SysRolePermission();
                        item.setRoleId(param.getRoleId());
                        item.setPermissionId(permissionId);
                        item.setCreateBy(currentUserId);
                        item.setUpdateBy(currentUserId);
                        item.setCreateTime(now);
                        item.setUpdateTime(now);
                        item.setDeleted(0);
                        return item;
                    })
                    .collect(Collectors.toList());

            boolean saveBatch = sysRolePermissionService.saveBatch(saveList);
            if (!saveBatch) {
                throw new SystemException("配置权限失败");
            }
        }

        return Resp.success("保存成功", null);
    }
}
