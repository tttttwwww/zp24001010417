package com.recruitment.controller;

import com.recruitment.common.Result;
import com.recruitment.model.Resume;
import com.recruitment.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历模块（按课件 2.3 编写）——Service 写好后，Controller 只负责绑参并调用 Service。
 *
 * 方法      路径                       绑参
 * POST      /resumes                   @RequestBody        新建
 * GET       /resumes/{id}              @PathVariable       详情
 * PUT       /resumes/{id}              路径 id + JSON      修改（部分字段）
 * DELETE    /resumes/{id}              @PathVariable       删除
 * GET       /resumes/list?userId=      @RequestParam       某用户的列表
 */
@RestController
@RequestMapping("/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    /** 创建：POST，无子路径；参数从请求体 JSON 绑定 */
    @PostMapping
    public Result<Resume> create(@RequestBody Resume resume) {
        return resumeService.create(resume);
    }

    /** 详情：GET，路径带 {id}；id 从 URL 路径取 */
    @GetMapping("/{id}")
    public Result<Resume> getById(@PathVariable("id") Long id) {
        return resumeService.getById(id);
    }

    /** 修改：PUT，路径带 {id}；路径 id + 请求体 JSON（部分字段） */
    @PutMapping("/{id}")
    public Result<Resume> update(@PathVariable("id") Long id, @RequestBody Resume resume) {
        return resumeService.update(id, resume);
    }

    /** 删除：DELETE，路径带 {id}；id 从 URL 路径取 */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") Long id) {
        return resumeService.delete(id);
    }

    /** 列表：GET，子路径用 /list；userId 从 ?userId= 取 */
    @GetMapping("/list")
    public Result<List<Resume>> listByUserId(@RequestParam("userId") Long userId) {
        return resumeService.listByUserId(userId);
    }
}
