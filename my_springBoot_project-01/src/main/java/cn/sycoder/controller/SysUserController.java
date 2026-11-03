package cn.sycoder.controller;

import cn.sycoder.auth.AuthInfo;
import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysDept;
import cn.sycoder.domain.SysRole;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.DataAccessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.LoginParam;
import cn.sycoder.param.UserChangeDeptParam;
import cn.sycoder.param.UserListParam;
import cn.sycoder.param.UserPwdParam;
import cn.sycoder.param.UserSaveParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.LoginResponse;
import cn.sycoder.response.UserResponse;
import cn.sycoder.service.ISysDeptService;
import cn.sycoder.service.ISysRoleService;
import cn.sycoder.service.ISysUserService;
import cn.sycoder.service.RedisSessionService;
import cn.sycoder.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
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

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author sy
 * @since 2026-03-26
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/api")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private RedisSessionService redisSessionService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Resp<LoginResponse> login(LoginParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getUsername() == null || param.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (param.getPassword() == null || param.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, param.getUsername().trim());

        SysUser sysUser = sysUserService.getOne(wrapper);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        if ("Y".equals(sysUser.getIsLock())) {
            throw new BusinessException("用户已被锁定");
        }
        if (!param.getPassword().equals(sysUser.getUserPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(sysUser, response);

        String token;
        try {
            token = JwtUtils.generateToken(sysUser);
        } catch (Exception e) {
            throw new SystemException("生成登录令牌失败", e);
        }
        response.setToken(token);

        long now = System.currentTimeMillis();
        AuthInfo authInfo = new AuthInfo();
        authInfo.setUserId(sysUser.getUserId());
        authInfo.setUserName(sysUser.getUserName());
        authInfo.setUserRealName(sysUser.getUserRealName());
        authInfo.setRoleId(sysUser.getRoleId());
        authInfo.setDeptId(sysUser.getDeptId());
        authInfo.setIsLock(sysUser.getIsLock());
        authInfo.setToken(token);
        authInfo.setLoginTime(now);
        authInfo.setExpireTime(now + redisSessionService.getTokenTtlMillis());

        try {
            redisSessionService.saveAuthInfo(token, authInfo);
        } catch (Exception e) {
            throw new DataAccessException("登录会话保存失败", e);
        }

        System.out.println(response);
        return Resp.success("登录成功", response);
    }

    @PostMapping("/User/list")
    @ApiOperation("用户列表")
    public Resp<List<UserResponse>> userList(UserListParam param) {
        if (param == null) {
            param = new UserListParam();
        }

        int pageNum = (param.getPage() == null || param.getPage() < 1) ? 1 : param.getPage();
        int pageSize = (param.getLimit() == null || param.getLimit() < 1) ? 10 : param.getLimit();

        Page<UserResponse> pageInfo = new Page<>(pageNum, pageSize);

        MPJLambdaWrapper<SysUser> wrapper = new MPJLambdaWrapper<SysUser>()
                .selectAll(SysUser.class)
                .select("t1.dept_name as deptName")
                .select("t2.role_name as roleName")
                .select("t3.user_name as addUser")
                .select("t4.user_name as editUser")
                .leftJoin(SysDept.class, "t1", SysDept::getDeptId, SysUser::getDeptId)
                .leftJoin(SysRole.class, "t2", SysRole::getRoleId, SysUser::getRoleId)
                .leftJoin(SysUser.class, "t3", SysUser::getUserId, SysUser::getCreateBy)
                .leftJoin(SysUser.class, "t4", SysUser::getUserId, SysUser::getUpdateBy)
                .eq(param.getDeptId() != null, SysUser::getDeptId, param.getDeptId())
                .like(param.getUserName() != null && !param.getUserName().trim().isEmpty(),
                        SysUser::getUserName, param.getUserName().trim())
                .like(param.getUserMobile() != null && !param.getUserMobile().trim().isEmpty(),
                        SysUser::getUserMobile, param.getUserMobile().trim())
                .eq(param.getIsLock() != null && !param.getIsLock().trim().isEmpty(),
                        SysUser::getIsLock, param.getIsLock().trim());

        IPage<UserResponse> result = sysUserService.selectJoinListPage(pageInfo, UserResponse.class, wrapper);

        return Resp.success("查询成功", result.getRecords(), result.getTotal());
    }

    @PostMapping("/User/save")
    @ApiOperation("用户新增/编辑")
    public Resp<Object> userSave(UserSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getUserName() == null || param.getUserName().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (param.getUserRealName() == null || param.getUserRealName().trim().isEmpty()) {
            throw new BusinessException("真实姓名不能为空");
        }
        if (param.getRoleId() == null) {
            throw new BusinessException("角色不能为空");
        }
        if (param.getDeptId() == null) {
            throw new BusinessException("部门不能为空");
        }
        if (param.getUserMobile() == null || param.getUserMobile().trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (param.getUserEmail() == null || param.getUserEmail().trim().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        if (param.getUserSex() == null || param.getUserSex().trim().isEmpty()) {
            throw new BusinessException("性别不能为空");
        }

        LambdaQueryWrapper<SysUser> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysUser::getUserName, param.getUserName().trim());
        checkWrapper.ne(param.getUserId() != null, SysUser::getUserId, param.getUserId());

        long count = sysUserService.count(checkWrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysRole role = sysRoleService.getById(param.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        SysDept dept = sysDeptService.getById(param.getDeptId());
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        LocalDateTime now = LocalDateTime.now();

        if (param.getUserId() == null) {
            SysUser user = new SysUser();
            user.setUserName(param.getUserName().trim());
            user.setUserRealName(param.getUserRealName().trim());
            user.setRoleId(param.getRoleId());
            user.setDeptId(param.getDeptId());
            user.setUserMobile(param.getUserMobile().trim());
            user.setUserEmail(param.getUserEmail().trim());
            user.setUserSex(param.getUserSex().trim());
            user.setUserPassword("123456");
            user.setIsLock("N");
            user.setDeleted(0);
            user.setCreateBy(currentUserId);
            user.setUpdateBy(currentUserId);
            user.setCreateTime(now);
            user.setUpdateTime(now);

            boolean save = sysUserService.save(user);
            if (!save) {
                throw new SystemException("新增失败");
            }
            return Resp.success("新增成功", null);
        }

        SysUser user = sysUserService.getById(param.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setUserName(param.getUserName().trim());
        user.setUserRealName(param.getUserRealName().trim());
        user.setRoleId(param.getRoleId());
        user.setDeptId(param.getDeptId());
        user.setUserMobile(param.getUserMobile().trim());
        user.setUserEmail(param.getUserEmail().trim());
        user.setUserSex(param.getUserSex().trim());
        user.setUpdateBy(currentUserId);
        user.setUpdateTime(now);

        boolean update = sysUserService.updateById(user);
        if (!update) {
            throw new SystemException("修改失败");
        }
        return Resp.success("修改成功", null);
    }

    @PostMapping("/User/pwd")
    @ApiOperation("重置密码")
    public Resp<Object> userPwd(UserPwdParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        SysUser user = sysUserService.getById(param.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setUserPassword("123456");
        SysUser currentUser = UserContext.get();
        user.setUpdateBy(currentUser != null ? currentUser.getUserId() : null);
        user.setUpdateTime(LocalDateTime.now());

        boolean update = sysUserService.updateById(user);
        if (!update) {
            throw new SystemException("重置密码失败");
        }
        return Resp.success("重置密码成功", null);
    }

    @DeleteMapping("/User/delete")
    @ApiOperation("删除用户")
    public Resp<Object> userDelete(String ids) {
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
            throw new BusinessException("用户ID格式错误");
        }

        if (idList.isEmpty()) {
            throw new BusinessException("没有可删除的用户ID");
        }

        boolean remove = sysUserService.removeByIds(idList);
        if (!remove) {
            throw new SystemException("删除失败");
        }
        return Resp.success("删除成功", null);
    }

    @GetMapping("/User/lock")
    @ApiOperation("用户锁定/解锁")
    public Resp<Object> userLock(Long userId, String lock) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (lock == null || (!"Y".equals(lock) && !"N".equals(lock))) {
            throw new BusinessException("锁定状态参数错误");
        }

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setIsLock(lock);
        SysUser currentUser = UserContext.get();
        user.setUpdateBy(currentUser != null ? currentUser.getUserId() : null);
        user.setUpdateTime(LocalDateTime.now());

        boolean update = sysUserService.updateById(user);
        if (!update) {
            throw new SystemException("状态修改失败");
        }
        return Resp.success("状态修改成功", null);
    }

    @PostMapping("/User/change/dept")
    @ApiOperation("批量修改用户部门")
    public Resp<Object> changeDept(UserChangeDeptParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getUserIds() == null || param.getUserIds().trim().isEmpty()) {
            throw new BusinessException("用户ID不能为空");
        }
        if (param.getDeptId() == null) {
            throw new BusinessException("部门不能为空");
        }

        SysDept dept = sysDeptService.getById(param.getDeptId());
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        List<Long> userIds;
        try {
            userIds = Arrays.stream(param.getUserIds().split(","))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new BusinessException("用户ID格式错误");
        }

        if (userIds.isEmpty()) {
            throw new BusinessException("没有可修改的用户");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getUserId, userIds);
        List<SysUser> users = sysUserService.list(wrapper);
        if (users.isEmpty()) {
            throw new BusinessException("用户不存在");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        LocalDateTime now = LocalDateTime.now();

        users.forEach(user -> {
            user.setDeptId(param.getDeptId());
            user.setUpdateBy(currentUserId);
            user.setUpdateTime(now);
        });

        boolean update = sysUserService.updateBatchById(users);
        if (!update) {
            throw new SystemException("部门设置失败");
        }
        return Resp.success("部门设置成功", null);
    }
}
