package com.recruitment.service.impl;

import com.recruitment.common.Result;
import com.recruitment.data.MockDataStore;
import com.recruitment.model.User;
import com.recruitment.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户模块实现（按课件 1.2 编写）
 */
@Service
public class UserServiceImpl implements UserService {

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
        if (MockDataStore.getUserByUsername(request.getUserName()) != null) {
            return Result.error("用户名已存在");
        }

        // 第 3 步：真正「新建」一个用户对象，不能直接把 request 存进去
        // （request 只是前端传来的原始数据，id、创建时间这些字段要由后端自己生成）
        User user = new User();
        user.setId(MockDataStore.nextUserId());          // id 由仓库统一生成，不能让前端传
        user.setUserName(request.getUserName());
        user.setPassWord(request.getPassWord());
        user.setNickName(request.getNickName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCreateTime(LocalDateTime.now());          // 创建时间以服务器时间为准

        // 第 4 步：存进内存仓库
        MockDataStore.saveUser(user);

        // 第 5 步：返回给前端之前，把密码去掉再返回（安全起见，密码不能明文回传）
        return Result.success("注册成功", maskPassword(user));
    }

    @Override
    public Result<User> login(User request) {
        // 第 1 步：同样先校验参数不能为空
        if (request == null || isBlank(request.getUserName())) {
            return Result.error("用户名不能为空");
        }
        if (isBlank(request.getPassWord())) {
            return Result.error("密码不能为空");
        }

        // 第 2 步：按用户名去仓库里找这个人
        User user = MockDataStore.getUserByUsername(request.getUserName());

        // 第 3 步：人不存在 或者 密码不对，统一提示「用户名或密码错误」
        // 故意不区分「用户不存在」和「密码错了」两种提示，避免被用来枚举出哪些用户名已注册
        if (user == null || !user.getPassWord().equals(request.getPassWord())) {
            return Result.error("用户名或密码错误");
        }

        // 第 4 步：登录成功，同样去掉密码再返回
        return Result.success("登录成功", maskPassword(user));
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        // 按 id 查；查不到直接返回业务错误
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        User user = MockDataStore.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(maskPassword(user));
    }

    /**
     * 把 User 里的密码字段「隐藏」掉，返回一份新的、没有密码的对象。
     * 注意：不是直接把 user.setPassWord(null) 改原对象，而是复制一份——
     * 避免不小心把内存仓库里那份「真实数据」的密码也清空了。
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
