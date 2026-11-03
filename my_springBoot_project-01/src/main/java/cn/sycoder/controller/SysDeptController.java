package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysDept;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.DeptListParam;
import cn.sycoder.param.DeptSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.DeptResponse;
import cn.sycoder.response.OptionResponse;
import cn.sycoder.service.ISysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "部门接口")
@RestController
@RequestMapping("/api")
public class SysDeptController {

    @Autowired
    private ISysDeptService sysDeptService;

    @PostMapping("/Dept/list")
    @ApiOperation("部门列表")
    public Resp<List<DeptResponse>> deptList(DeptListParam param) {
        if (param == null) {
            param = new DeptListParam();
        }

        int pageNum = (param.getPage() == null || param.getPage() < 1) ? 1 : param.getPage();
        int pageSize = (param.getLimit() == null || param.getLimit() < 1) ? 10 : param.getLimit();

        MPJLambdaWrapper<SysDept> wrapper = new MPJLambdaWrapper<SysDept>()
                .selectAll(SysDept.class)
                .select("t1.user_name as addUser")
                .select("t2.user_name as editUser")
                .leftJoin(SysUser.class, "t1", SysUser::getUserId, SysDept::getCreateBy)
                .leftJoin(SysUser.class, "t2", SysUser::getUserId, SysDept::getUpdateBy)
                .like(param.getDeptName() != null && !param.getDeptName().trim().isEmpty(),
                        SysDept::getDeptName, param.getDeptName().trim())
                .like(param.getDeptNo() != null && !param.getDeptNo().trim().isEmpty(),
                        SysDept::getDeptNo, param.getDeptNo().trim())
                .orderByDesc(SysDept::getUpdateTime);

        Page<DeptResponse> page = new Page<>(pageNum, pageSize);
        IPage<DeptResponse> result = sysDeptService.selectJoinListPage(page, DeptResponse.class, wrapper);

        return Resp.success("查询成功", result.getRecords(), result.getTotal());
    }

    @GetMapping("/Dept/dropDown/all")
    @ApiOperation("部门下拉")
    public Resp<List<OptionResponse>> deptDropDownAll() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysDept::getDeptId, SysDept::getDeptName)
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getDeptId);

        List<OptionResponse> result = sysDeptService.list(wrapper).stream()
                .map(dept -> {
                    OptionResponse response = new OptionResponse();
                    response.setId(dept.getDeptId());
                    response.setName(dept.getDeptName());
                    return response;
                })
                .collect(Collectors.toList());

        return Resp.success("查询成功", result, (long) result.size());
    }

    @PostMapping("/Dept/save")
    @ApiOperation("部门新增/编辑")
    public Resp<Object> deptSave(DeptSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getDeptName() == null || param.getDeptName().trim().isEmpty()) {
            throw new BusinessException("部门名称不能为空");
        }
        if (param.getDeptNo() == null || param.getDeptNo().trim().isEmpty()) {
            throw new BusinessException("部门代码不能为空");
        }

        LambdaQueryWrapper<SysDept> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysDept::getDeptNo, param.getDeptNo().trim());
        checkWrapper.ne(param.getDeptId() != null, SysDept::getDeptId, param.getDeptId());

        long count = sysDeptService.count(checkWrapper);
        if (count > 0) {
            throw new BusinessException("部门代码已存在");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;

        if (param.getDeptId() == null) {
            SysDept dept = new SysDept();
            LocalDateTime now = LocalDateTime.now();
            dept.setDeptName(param.getDeptName().trim());
            dept.setDeptNo(param.getDeptNo().trim());
            dept.setCreateBy(currentUserId);
            dept.setUpdateBy(currentUserId);
            dept.setCreateTime(now);
            dept.setUpdateTime(now);

            boolean save = sysDeptService.save(dept);
            if (!save) {
                throw new SystemException("新增失败");
            }
            return Resp.success("新增成功", null);
        }

        SysDept dept = sysDeptService.getById(param.getDeptId());
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        dept.setDeptName(param.getDeptName().trim());
        dept.setDeptNo(param.getDeptNo().trim());
        dept.setUpdateBy(currentUserId);
        dept.setUpdateTime(LocalDateTime.now());

        boolean update = sysDeptService.updateById(dept);
        if (!update) {
            throw new SystemException("修改失败");
        }
        return Resp.success("修改成功", null);
    }

    @GetMapping("/Dept/delete")
    @ApiOperation("删除部门")
    public Resp<Object> deptDelete(String ids) {
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
            throw new BusinessException("部门ID格式错误");
        }

        if (idList.isEmpty()) {
            throw new BusinessException("没有可删除的部门ID");
        }

        boolean remove = sysDeptService.removeByIds(idList);
        if (!remove) {
            throw new SystemException("删除失败");
        }
        return Resp.success("删除成功", null);
    }
}
