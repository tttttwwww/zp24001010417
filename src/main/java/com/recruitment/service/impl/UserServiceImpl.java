package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.common.Result;
import com.recruitment.mapper.UserMapper;
import com.recruitment.model.User;
import com.recruitment.service.UserService;
import com.recruitment.util.JwtUtil;
import com.recruitment.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户模块实现（第03步：数据源从 MockDataStore 换成 UserMapper）
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 【第06步新增】登录成功后要生成 Token，所以注入 JwtUtil
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<User> register(User request) {
        // 第 1 步：校验参数——用户名、密码不能为空，为空就直接返回错误，不往下走
        if (request == null || isBlank(request.getUserName())) {
            return Result.error("用户名不能为空");
        }
        if (isBlank(request.getPassWord())) {
            return Result.error("密码不能为空");
        }

        // 第 2 步：查重——用户名已存在就不能再注册一个同名的
        // SELECT ... FROM user WHERE user_name = ? LIMIT 1
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, request.getUserName());
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error("用户名已存在");
        }

        // 第 3 步：真正「新建」一个用户对象，id 交给数据库自增，创建时间取服务器时间
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassWord(request.getPassWord());
        user.setNickName(request.getNickName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCreateTime(LocalDateTime.now());

        // 第 4 步：插入 MySQL；insert 成功后 user.getId() 会被回填
        userMapper.insert(user);

        // 第 5 步：返回给前端之前，把密码去掉再返回
        return Result.success("注册成功", maskPassword(user));
    }

    @Override
    public Result<LoginVO> login(User request) {
        // 第 1 步：同样先校验参数不能为空
        if (request == null || isBlank(request.getUserName())) {
            return Result.error("用户名不能为空");
        }
        if (isBlank(request.getPassWord())) {
            return Result.error("密码不能为空");
        }

        // 第 2 步：按用户名去库里找这个人
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, request.getUserName());
        User user = userMapper.selectOne(wrapper);

        // 第 3 步：人不存在 或者 密码不对，统一提示，避免被枚举账号
        if (user == null || !user.getPassWord().equals(request.getPassWord())) {
            return Result.error("用户名或密码错误");
        }

        // 第 4 步：用数据库中的用户 ID 生成 Token
        String token = jwtUtil.generateToken(user.getId());

        // 第 5 步：同时返回 Token 和已去掉密码的用户信息
        return Result.success("登录成功", new LoginVO(token, maskPassword(user)));
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        // 按 id 查；查不到直接返回业务错误
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(maskPassword(user));
    }

    /**
     * 把 User 里的密码字段「隐藏」掉，返回一份新的、没有密码的对象。
     * 注意：不是直接把 user.setPassWord(null) 改原对象，而是复制一份——
     * 避免不小心把数据库里那份「真实数据」的密码也清空了。
     */
    private User maskPassword(User user) {
        User safe = new User();
        safe.setId(user.getId());
        safe.setUserName(user.getUserName());
        safe.setNickName(user.getNickName());
        safe.setEmail(user.getEmail());
        safe.setPhone(user.getPhone());
        safe.setCreateTime(user.getCreateTime());
        // 故意不 set passWord，safe.passWord 保持 null
        return safe;
    }

    /** 判断字符串是否「空」：null，或者去掉首尾空格后是空串，两种情况都算空 */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
