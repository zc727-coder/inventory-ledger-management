package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysModule;
import cn.sycoder.domain.SysRoleModule;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.ModuleSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.ModuleGetResponse;
import cn.sycoder.response.ModuleNodesResponse;
import cn.sycoder.response.ModuleResponse;
import cn.sycoder.service.ISysModuleService;
import cn.sycoder.service.ISysRoleModuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "菜单接口")
@RestController
@RequestMapping("/api")
public class SysModuleController {

    @Autowired
    private ISysModuleService sysModuleService;

    @Autowired
    private ISysRoleModuleService sysRoleModuleService;

    @PostMapping("/Module/list")
    @ApiOperation("菜单列表")
    public Resp<List<ModuleResponse>> moduleList() {
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

        List<ModuleResponse> list = sysModuleService.selectJoinList(ModuleResponse.class, wrapper);

        list.forEach(m -> {
            if (m.getOpen() == null) {
                m.setOpen(true);
            }
            if (m.getChecked() == null) {
                m.setChecked(false);
            }
        });

        return Resp.success("查询成功", list, (long) list.size());
    }

    @PostMapping("/Module/nodes")
    @ApiOperation("父级菜单下拉")
    public Resp<List<ModuleNodesResponse>> moduleNodes() {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysModule::getModuleId, SysModule::getModuleName)
                .orderByAsc(SysModule::getModuleOrder);

        List<SysModule> list = sysModuleService.list(wrapper);

        List<ModuleNodesResponse> result = list.stream()
                .map(m -> {
                    ModuleNodesResponse r = new ModuleNodesResponse();
                    r.setId(m.getModuleId());
                    r.setName(m.getModuleName());
                    return r;
                })
                .collect(Collectors.toList());

        return Resp.success("查询成功", result, (long) result.size());
    }

    @GetMapping("/Module/get/{id}")
    @ApiOperation("获取菜单详情")
    public Resp<ModuleGetResponse> moduleGet(@PathVariable Long id) {
        SysModule module = sysModuleService.getById(id);
        if (module == null) {
            throw new BusinessException("菜单不存在");
        }

        ModuleGetResponse response = new ModuleGetResponse();
        response.setModuleId(module.getModuleId());
        response.setParentId(module.getParentId());
        response.setModuleName(module.getModuleName());
        response.setModuleUrl(module.getModuleUrl());
        response.setModuleIcon(module.getModuleIcon());
        response.setModuleOrder(module.getModuleOrder());
        response.setModuleLevel(module.getModuleLevel());
        response.setSystemNo(module.getSystemNo());
        response.setIsLeaf(module.getIsLeaf());
        response.setFullIndex(module.getFullIndex());
        response.setModuleNotes(module.getModuleNotes());
        response.setCreateTime(module.getCreateTime());
        response.setUpdateTime(module.getUpdateTime());

        return Resp.success("查询成功", response);
    }

    @PostMapping("/Module/save")
    @ApiOperation("菜单新增/编辑")
    public Resp<Object> moduleSave(ModuleSaveParam param) {
        System.out.println(param);
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getModuleName() == null || param.getModuleName().trim().isEmpty()) {
            throw new BusinessException("菜单名称不能为空");
        }
        if (param.getParentId() == null) {
            param.setParentId(0L);
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;

        if (param.getModuleId() == null) {
            SysModule module = new SysModule();
            module.setParentId(param.getParentId());
            module.setModuleName(param.getModuleName().trim());
            module.setModuleIcon(param.getModuleIcon());
            module.setModuleUrl(param.getModuleUrl());
            module.setModuleOrder(param.getModuleOrder() != null ? param.getModuleOrder() : 0);
            module.setSystemNo(param.getSystemNo());
            module.setModuleNotes(param.getModuleNotes());
            module.setCreateBy(currentUserId);
            module.setUpdateBy(currentUserId);

            if (param.getParentId() == 0L) {
                module.setModuleLevel(1);
            } else {
                SysModule parent = sysModuleService.getById(param.getParentId());
                module.setModuleLevel(parent != null ? parent.getModuleLevel() + 1 : 1);
            }

            boolean save = sysModuleService.save(module);
            if (!save) {
                throw new SystemException("新增失败");
            }
            return Resp.success("新增成功", null);
        }

        SysModule module = sysModuleService.getById(param.getModuleId());
        if (module == null) {
            throw new BusinessException("菜单不存在");
        }

        module.setParentId(param.getParentId());
        module.setModuleName(param.getModuleName().trim());
        module.setModuleIcon(param.getModuleIcon());
        module.setModuleUrl(param.getModuleUrl());
        module.setModuleOrder(param.getModuleOrder() != null ? param.getModuleOrder() : 0);
        module.setSystemNo(param.getSystemNo());
        module.setModuleNotes(param.getModuleNotes());
        module.setUpdateBy(currentUserId);
        module.setUpdateTime(LocalDateTime.now());

        boolean update = sysModuleService.updateById(module);
        if (!update) {
            throw new SystemException("修改失败");
        }
        return Resp.success("修改成功", null);
    }

    @DeleteMapping("/Module/delete")
    @ApiOperation("删除菜单")
    public Resp<Object> moduleDelete(@RequestParam String ids) {
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
            throw new BusinessException("菜单ID格式错误");
        }

        if (idList.isEmpty()) {
            throw new BusinessException("没有可删除的菜单ID");
        }

        System.out.println("=== 删除菜单，菜单ID列表: " + idList);

        LambdaQueryWrapper<SysRoleModule> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(SysRoleModule::getModuleId, idList);
        System.out.println("=== 删除条件: module_id in " + idList);
        boolean removeRoleModule = sysRoleModuleService.remove(deleteWrapper);
        System.out.println("=== 删除关联表结果: " + removeRoleModule);

        boolean remove = sysModuleService.removeByIds(idList);
        System.out.println("=== 删除菜单结果: " + remove);
        if (!remove) {
            throw new SystemException("删除失败");
        }
        return Resp.success("删除成功", null);
    }
}
