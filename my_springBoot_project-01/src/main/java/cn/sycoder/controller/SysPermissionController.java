package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysPermission;
import cn.sycoder.domain.SysRolePermission;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.PermissionListParam;
import cn.sycoder.param.PermissionSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.PermissionResponse;
import cn.sycoder.service.ISysPermissionService;
import cn.sycoder.service.ISysRolePermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "权限接口")
@RestController
@RequestMapping("/api")
public class SysPermissionController {

    @Autowired
    private ISysPermissionService sysPermissionService;

    @Autowired
    private ISysRolePermissionService sysRolePermissionService;

    @PostMapping("/Permission/list")
    @ApiOperation("权限列表")
    public Resp<List<PermissionResponse>> permissionList(PermissionListParam param) {
        if (param == null) {
            param = new PermissionListParam();
        }

        int pageNum = (param.getPage() == null || param.getPage() < 1) ? 1 : param.getPage();
        int pageSize = (param.getLimit() == null || param.getLimit() < 1) ? 10 : param.getLimit();

        MPJLambdaWrapper<SysPermission> wrapper = new MPJLambdaWrapper<SysPermission>()
                .selectAll(SysPermission.class)
                .select("t1.user_name as addUser")
                .select("t2.user_name as editUser")
                .leftJoin(SysUser.class, "t1", SysUser::getUserId, SysPermission::getCreateBy)
                .leftJoin(SysUser.class, "t2", SysUser::getUserId, SysPermission::getUpdateBy)
                .like(param.getPermissionName() != null && !param.getPermissionName().trim().isEmpty(),
                        SysPermission::getPermissionName, param.getPermissionName().trim())
                .like(param.getPermission() != null && !param.getPermission().trim().isEmpty(),
                        SysPermission::getPermission, param.getPermission().trim())
                .orderByDesc(SysPermission::getUpdateTime);

        Page<PermissionResponse> page = new Page<>(pageNum, pageSize);
        IPage<PermissionResponse> result = sysPermissionService.selectJoinListPage(page, PermissionResponse.class, wrapper);

        Long roleId = param.getRoleId();
        if (roleId != null && roleId > 0 && result.getRecords() != null && !result.getRecords().isEmpty()) {
            LambdaQueryWrapper<SysRolePermission> rolePermissionWrapper = new LambdaQueryWrapper<>();
            rolePermissionWrapper.eq(SysRolePermission::getRoleId, roleId);
            List<Long> selectedPermissionIds = sysRolePermissionService.list(rolePermissionWrapper).stream()
                    .map(SysRolePermission::getPermissionId)
                    .collect(Collectors.toList());

            result.getRecords().forEach(item -> item.setChecked(selectedPermissionIds.contains(item.getPermissionId())));
        }

        return Resp.success("查询成功", result.getRecords(), result.getTotal());
    }

    @PostMapping("/Permission/save")
    @ApiOperation("权限新增/编辑")
    public Resp<Object> permissionSave(PermissionSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getPermissionName() == null || param.getPermissionName().trim().isEmpty()) {
            throw new BusinessException("权限名称不能为空");
        }
        if (param.getPermission() == null || param.getPermission().trim().isEmpty()) {
            throw new BusinessException("权限标识不能为空");
        }

        LambdaQueryWrapper<SysPermission> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysPermission::getPermission, param.getPermission().trim());
        checkWrapper.ne(param.getPermissionId() != null, SysPermission::getPermissionId, param.getPermissionId());
        long count = sysPermissionService.count(checkWrapper);
        if (count > 0) {
            throw new BusinessException("权限标识已存在");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        LocalDateTime now = LocalDateTime.now();

        if (param.getPermissionId() == null) {
            SysPermission permission = new SysPermission();
            permission.setPermissionName(param.getPermissionName().trim());
            permission.setPermission(param.getPermission().trim());
            permission.setPermissionNotes(param.getPermissionNotes() != null ? param.getPermissionNotes().trim() : null);
            permission.setCreateBy(currentUserId);
            permission.setUpdateBy(currentUserId);
            permission.setCreateTime(now);
            permission.setUpdateTime(now);
            permission.setDeleted(0);

            boolean save = sysPermissionService.save(permission);
            if (!save) {
                throw new SystemException("新增失败");
            }
            return Resp.success("新增成功", null);
        }

        SysPermission permission = sysPermissionService.getById(param.getPermissionId());
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        permission.setPermissionName(param.getPermissionName().trim());
        permission.setPermission(param.getPermission().trim());
        permission.setPermissionNotes(param.getPermissionNotes() != null ? param.getPermissionNotes().trim() : null);
        permission.setUpdateBy(currentUserId);
        permission.setUpdateTime(now);

        boolean update = sysPermissionService.updateById(permission);
        if (!update) {
            throw new SystemException("修改失败");
        }
        return Resp.success("修改成功", null);
    }

    @DeleteMapping("/Permission/delete")
    @ApiOperation("删除权限")
    public Resp<Object> permissionDelete(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            throw new BusinessException("ids不能为空");
        }

        List<Long> idList;
        try {
            idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new BusinessException("权限ID格式错误");
        }

        if (idList.isEmpty()) {
            throw new BusinessException("没有可删除的权限ID");
        }

        LambdaQueryWrapper<SysRolePermission> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.in(SysRolePermission::getPermissionId, idList);
        sysRolePermissionService.remove(relationWrapper);

        boolean remove = sysPermissionService.removeByIds(idList);
        if (!remove) {
            throw new SystemException("删除失败");
        }
        return Resp.success("删除成功", null);
    }
}
