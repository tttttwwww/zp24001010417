package com.recruitment.service;

import com.recruitment.common.Result;
import com.recruitment.model.Resume;

import java.util.List;

/**
 * 简历模块：一套完整 CRUD（按课件 2.1 编写）
 */
public interface ResumeService {

    // 新建简历：参数是前端 POST 传来的 Resume（JSON），返回创建后的完整简历
    Result<Resume> create(Resume resume);

    // 按简历 id 查详情（GET /resumes/{id}）
    Result<Resume> getById(Long id);

    // 按 id 修改：路径上的 id 为准，请求体里只传要改的字段（部分更新）
    Result<Resume> update(Long id, Resume resume);

    // 按 id 删除一条简历
    Result<String> delete(Long id);

    // 查某个用户的全部简历（GET /resumes/list?userId=）
    Result<List<Resume>> listByUserId(Long userId);
}
