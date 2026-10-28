package com.recruitment.controller;

import com.recruitment.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 根路径索引：访问 http://localhost:8080/api 或 http://localhost:8080/api/
 * 时不再落到 Whitelabel Error Page（No static resource .），改为返回一份接口清单。
 *
 * 只是开发期的小便利，不影响课件要求的任何业务接口。
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public Result<Map<String, Object>> index() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "招聘网站后端");
        info.put("step", "第03步：MyBatis Plus 集成（MySQL 持久化）");
        info.put("baseUrl", "http://localhost:8080/api");

        List<Map<String, String>> apis = new ArrayList<>();
        apis.add(api("GET",    "/hello",                                  "欢迎语（自检用）"));
        apis.add(api("GET",    "/jobs/list?city=&keyword=",               "职位列表 / 双条件筛选"));
        apis.add(api("GET",    "/jobs/{id}",                              "职位详情，viewCount +1"));
        apis.add(api("POST",   "/users/register",                         "注册，@RequestBody"));
        apis.add(api("POST",   "/users/login",                            "登录，@RequestBody"));
        apis.add(api("GET",    "/users/{id}",                             "用户信息"));
        apis.add(api("POST",   "/resumes",                                "新建简历"));
        apis.add(api("GET",    "/resumes/{id}",                           "简历详情"));
        apis.add(api("PUT",    "/resumes/{id}",                           "部分更新简历"));
        apis.add(api("GET",    "/resumes/list?userId=",                   "某用户简历列表"));
        apis.add(api("DELETE", "/resumes/{id}",                           "删除简历"));
        apis.add(api("POST",   "/favorites/add",                          "收藏职位"));
        apis.add(api("DELETE", "/favorites/remove?userId=&jobId=",        "取消收藏"));
        apis.add(api("GET",    "/favorites/list?userId=",                 "收藏列表（返 Job）"));
        apis.add(api("POST",   "/applications/apply",                     "投递职位"));
        apis.add(api("PUT",    "/applications/cancel?userId=&jobId=",     "取消投递（只改状态）"));
        apis.add(api("GET",    "/applications/list?userId=",              "投递列表"));
        info.put("apis", apis);

        return Result.success("招聘网站后端运行中，完整接口见 data.apis", info);
    }

    private Map<String, String> api(String method, String path, String desc) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("method", method);
        item.put("path", path);
        item.put("desc", desc);
        return item;
    }
}
