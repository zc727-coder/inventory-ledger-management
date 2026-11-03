package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysRole;
import cn.sycoder.domain.SysRoleModule;
import cn.sycoder.domain.SysRolePermission;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.RoleListParam;
import cn.sycoder.param.RoleSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.OptionResponse;
import cn.sycoder.response.RoleResponse;
import cn.sycoder.service.ISysRoleModuleService;
import cn.sycoder.service.ISysRolePermissionService;
import cn.sycoder.service.ISysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "角色接口")
@RestController
@RequestMapping("/api")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysRoleModuleService sysRoleModuleService;

    @Autowired
    private ISysRolePermissionService sysRolePermissionService;

    @PostMapping("/Role/list")
    @ApiOperation("角色列表")
    public Resp<List<RoleResponse>> roleList(RoleListParam param) {
        if (param == null) {
            param = new RoleListParam();
        }

        int pageNum = (param.getPage() == null || param.getPage() < 1) ? 1 : param.getPage();
        int pageSize = (param.getLimit() == null || param.getLimit() < 1) ? 10 : param.getLimit();

        MPJLambdaWrapper<SysRole> wrapper = new MPJLambdaWrapper<SysRole>()
                .selectAll(SysRole.class)
                .select("t1.user_name as addUser")
                .select("t2.user_name as editUser")
                .leftJoin(SysUser.class, "t1", SysUser::getUserId, SysRole::getCreateBy)
                .leftJoin(SysUser.class, "t2", SysUser::getUserId, SysRole::getUpdateBy)
                .like(param.getRoleName() != null && !param.getRoleName().trim().isEmpty(),
                        SysRole::getRoleName, param.getRoleName().trim())
                .like(param.getRoleNo() != null && !param.getRoleNo().trim().isEmpty(),
                        SysRole::getRoleNo, param.getRoleNo().trim())
                .orderByDesc(SysRole::getUpdateTime);

        Page<RoleResponse> page = new Page<>(pageNum, pageSize);
        IPage<RoleResponse> result = sysRoleService.selectJoinListPage(page, RoleResponse.class, wrapper);

        return Resp.success("查询成功", result.getRecords(), result.getTotal());
    }

    @GetMapping("/Role/dropDown/all")
    @ApiOperation("角色下拉")
    public Resp<List<OptionResponse>> roleDropDownAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysRole::getRoleId, SysRole::getRoleName)
                .orderByAsc(SysRole::getRoleId);

        List<OptionResponse> result = sysRoleService.list(wrapper).stream()
                .map(role -> {
                    OptionResponse response = new OptionResponse();
                    response.setId(role.getRoleId());
                    response.setName(role.getRoleName());
                    return response;
                })
                .collect(Collectors.toList());

        return Resp.success("查询成功", result, (long) result.size());
    }

    @PostMapping("/Role/save")
    @ApiOperation("角色新增/编辑")
    public Resp<Object> roleSave(RoleSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getRoleName() == null || param.getRoleName().trim().isEmpty()) {
            throw new BusinessException("角色名称不能为空");
        }
        if (param.getRoleNo() == null || param.getRoleNo().trim().isEmpty()) {
            throw new BusinessException("角色代码不能为空");
        }

        LambdaQueryWrapper<SysRole> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysRole::getRoleNo, param.getRoleNo().trim());
        checkWrapper.ne(param.getRoleId() != null, SysRole::getRoleId, param.getRoleId());

        long count = sysRoleService.count(checkWrapper);
        if (count > 0) {
            throw new BusinessException("角色代码已存在");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;

        if (param.getRoleId() == null) {
            SysRole role = new SysRole();
            LocalDateTime now = LocalDateTime.now();
            role.setSystemNo(param.getSystemNo() != null ? param.getSystemNo().trim() : null);
            role.setRoleNo(param.getRoleNo().trim());
            role.setRoleName(param.getRoleName().trim());
            role.setCreateBy(currentUserId);
            role.setUpdateBy(currentUserId);
            role.setCreateTime(now);
            role.setUpdateTime(now);

            boolean save = sysRoleService.save(role);
            if (!save) {
                throw new SystemException("新增失败");
            }
            return Resp.success("新增成功", null);
        }

        SysRole role = sysRoleService.getById(param.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setSystemNo(param.getSystemNo() != null ? param.getSystemNo().trim() : null);
        role.setRoleNo(param.getRoleNo().trim());
        role.setRoleName(param.getRoleName().trim());
        role.setUpdateBy(currentUserId);
        role.setUpdateTime(LocalDateTime.now());

        boolean update = sysRoleService.updateById(role);
        if (!update) {
            throw new SystemException("修改失败");
        }
        return Resp.success("修改成功", null);
    }

    @DeleteMapping("/Role/delete")
    @ApiOperation("删除角色")
    public Resp<Object> roleDelete(String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            throw new BusinessException("ids不能为空");
        }

        List<Long> idList;
        try {
            idList = Arrays.stream(ids.split(","))
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new BusinessException("角色ID格式错误");
        }

        if (idList.isEmpty()) {
            throw new BusinessException("没有可删除的角色ID");
        }

        for (Long roleId : idList) {
            LambdaQueryWrapper<SysRoleModule> moduleDeleteWrapper = new LambdaQueryWrapper<>();
            moduleDeleteWrapper.eq(SysRoleModule::getRoleId, roleId);
            sysRoleModuleService.remove(moduleDeleteWrapper);

            LambdaQueryWrapper<SysRolePermission> permissionDeleteWrapper = new LambdaQueryWrapper<>();
            permissionDeleteWrapper.eq(SysRolePermission::getRoleId, roleId);
            sysRolePermissionService.remove(permissionDeleteWrapper);
        }

        boolean remove = sysRoleService.removeByIds(idList);
        if (!remove) {
            throw new SystemException("删除失败");
        }
        return Resp.success("删除成功", null);
    }
}
