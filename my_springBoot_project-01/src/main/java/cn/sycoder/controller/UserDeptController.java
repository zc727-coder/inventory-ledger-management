package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysDept;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.UserDeptSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.UserDeptTreeResponse;
import cn.sycoder.service.ISysDeptService;
import cn.sycoder.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "用户部门接口")
@RestController
@RequestMapping("/api")
public class UserDeptController {

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private ISysUserService sysUserService;

    @GetMapping("/UserDept/deptTree/{id}")
    @ApiOperation("部门设置树")
    public Resp<List<UserDeptTreeResponse>> deptTree(@PathVariable Long id) {
        List<UserDeptTreeResponse> result = buildDeptTree(null);
        return Resp.success("查询成功", result, (long) result.size());
    }

    @GetMapping("/UserDept/tree/{userId}")
    @ApiOperation("用户部门树")
    public Resp<List<UserDeptTreeResponse>> userDeptTree(@PathVariable Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<UserDeptTreeResponse> result = buildDeptTree(user.getDeptId());
        return Resp.success("查询成功", result, (long) result.size());
    }

    @PostMapping("/UserDept/save")
    @ApiOperation("保存用户部门")
    public Resp<Object> userDeptSave(UserDeptSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        Long deptId = parseFirstDeptId(param.getDeptIds());
        if (deptId == null) {
            throw new BusinessException("部门不能为空");
        }

        SysDept dept = sysDeptService.getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        SysUser user = sysUserService.getById(param.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        SysUser currentUser = UserContext.get();
        user.setDeptId(deptId);
        user.setUpdateBy(currentUser != null ? currentUser.getUserId() : null);
        user.setUpdateTime(LocalDateTime.now());

        boolean update = sysUserService.updateById(user);
        if (!update) {
            throw new SystemException("保存失败");
        }
        return Resp.success("保存成功", null);
    }

    private List<UserDeptTreeResponse> buildDeptTree(Long checkedDeptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getDeptId);

        List<SysDept> depts = sysDeptService.list(wrapper);
        List<UserDeptTreeResponse> result = new ArrayList<>();
        for (SysDept dept : depts) {
            UserDeptTreeResponse response = new UserDeptTreeResponse();
            response.setId(dept.getDeptId());
            response.setPId(dept.getParentId() == null ? 0L : dept.getParentId());
            response.setName(dept.getDeptName());
            response.setOpen(true);
            response.setChecked(checkedDeptId != null && checkedDeptId.equals(dept.getDeptId()));
            result.add(response);
        }
        return result;
    }

    private Long parseFirstDeptId(String deptIds) {
        if (deptIds == null || deptIds.trim().isEmpty()) {
            return null;
        }

        List<String> values = Arrays.stream(
                        deptIds.replace("[", "")
                                .replace("]", "")
                                .replace("\"", "")
                                .split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return null;
        }

        try {
            return Long.valueOf(values.get(0));
        } catch (NumberFormatException e) {
            throw new BusinessException("部门ID格式错误");
        }
    }
}
