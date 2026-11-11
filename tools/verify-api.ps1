# ============================================================
# 接口验证脚本（第02步起，第06步加入 JWT）
# 用法：先启动后端（mvn spring-boot:run 或 java -jar target/...jar），再执行
#   powershell -ExecutionPolicy Bypass -File tools\verify-api.ps1
# 说明：脚本按课件顺序执行（职位 → 用户 → 简历 → 收藏 → 投递 → JWT），
#       第06步起除白名单外的接口都要带 Authorization: Bearer <token>，
#       脚本会在登录后自动取 Token 注入后续请求。
#       脚本会改数据，重复执行前建议先重跑 src/main/resources/db/init.sql 还原。
# ============================================================
param([string]$BaseUrl = "http://localhost:8080/api")

$script:steps = @()
$script:pass = 0
$script:fail = 0
$script:token = $null

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [string]$Name,
        [switch]$NoAuth
    )
    $uri = "$BaseUrl$Path"
    $params = @{ Uri = $uri; Method = $Method; TimeoutSec = 30; UseBasicParsing = $true }
    if (-not $NoAuth -and $script:token) {
        $params.Headers = @{ Authorization = "Bearer $script:token" }
    }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Compress -Depth 5
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = "application/json; charset=utf-8"
    }
    try {
        $resp = Invoke-WebRequest @params
        $raw = [System.Text.Encoding]::UTF8.GetString($resp.RawContentStream.ToArray())
        # 有些接口（如 Swagger UI）返回 HTML，不一定是 JSON，解析失败不能算请求失败
        try { $json = $raw | ConvertFrom-Json } catch { $json = $null }
        $result = [pscustomobject]@{ HttpStatus = [int]$resp.StatusCode; Body = $json; Raw = $raw; Error = $null }
    } catch {
        $httpStatus = 0
        $raw = ""
        $json = $null
        $resp = $_.Exception.Response
        # Windows PowerShell 5.1 会把错误响应体缓存到 ErrorDetails.Message，
        # 此后再读 GetResponseStream() 已经是空的，所以优先用它
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $raw = $_.ErrorDetails.Message
        }
        if ($null -ne $resp) {
            $httpStatus = [int]$resp.StatusCode
            if (-not $raw) {
                try {
                    $sr = New-Object IO.StreamReader($resp.GetResponseStream(), [System.Text.Encoding]::UTF8)
                    $raw = $sr.ReadToEnd()
                } catch { }
            }
        }
        if ($raw) {
            try { $json = $raw | ConvertFrom-Json } catch { $json = $null }
        }
        $result = [pscustomobject]@{ HttpStatus = $httpStatus; Body = $json; Raw = $raw; Error = $_.Exception.Message }
    }
    return $result
}

function Check {
    param([string]$Name, [scriptblock]$Condition, $Actual, [string]$Expect)
    $ok = $false
    try { $ok = [bool](& $Condition) } catch { $ok = $false }
    if ($ok) {
        $script:pass++
        $script:steps += [pscustomobject]@{ Step = $Name; Result = "PASS"; Expect = $Expect; Actual = $Actual }
        Write-Host ("[PASS] {0}  ->  {1}" -f $Name, $Actual) -ForegroundColor Green
    } else {
        $script:fail++
        $script:steps += [pscustomobject]@{ Step = $Name; Result = "FAIL"; Expect = $Expect; Actual = $Actual }
        Write-Host ("[FAIL] {0}  ->  {1}  (预期: {2})" -f $Name, $Actual, $Expect) -ForegroundColor Red
    }
}

Write-Host "== 0. 根路径自检 ==" -ForegroundColor Cyan
$index = Invoke-Api -Method GET -Path "/" -Name "index"
Check "GET /api/ 根路径可访问（不再 404）" { $index.HttpStatus -eq 200 -and $index.Body.code -eq 200 } "HTTP $($index.HttpStatus), code=$($index.Body.code)" "HTTP 200"

Write-Host "`n== 5.1 职位模块 ==" -ForegroundColor Cyan
$r = Invoke-Api -Method GET -Path "/jobs/list"
Check "职位列表-全部" { $r.Body.code -eq 200 -and $r.Body.data.Count -eq 3 } ("{0} 条, code={1}" -f $r.Body.data.Count, $r.Body.code) "3 条"

$r = Invoke-Api -Method GET -Path "/jobs/list?city=%E6%9D%AD%E5%B7%9E"
Check "职位列表-城市杭州" { $r.Body.data.Count -eq 1 -and $r.Body.data[0].companyName -eq "阿里巴巴" } ("{0} 条, {1}" -f $r.Body.data.Count, $r.Body.data[0].companyName) "1 条(阿里巴巴)"

$r = Invoke-Api -Method GET -Path "/jobs/list?keyword=Java"
Check "职位列表-关键词 Java" { $r.Body.data.Count -eq 1 -and $r.Body.data[0].jobTitle -match "Java" } ("{0} 条, {1}" -f $r.Body.data.Count, $r.Body.data[0].jobTitle) "1 条(含 Java)"

$before = (Invoke-Api -Method GET -Path "/jobs/1").Body.data.viewCount
$r = Invoke-Api -Method GET -Path "/jobs/1"
Check "职位详情 id=1，viewCount +1" { $r.Body.code -eq 200 -and $r.Body.data.viewCount -eq ($before + 1) } ("viewCount {0} -> {1}" -f $before, $r.Body.data.viewCount) "每次 +1"

$r = Invoke-Api -Method GET -Path "/jobs/99"
Check "职位详情 id=99 不存在" { $r.Body.code -ne 200 -and $r.Body.message -eq "职位不存在" } ("code=$($r.Body.code), message=$($r.Body.message)") "职位不存在"

Write-Host "`n== 5.2 用户模块（@RequestBody）==" -ForegroundColor Cyan
$r = Invoke-Api -Method POST -Path "/users/register" -Body @{ userName = "user1"; passWord = "123456"; nickName = "用户1"; email = "user1@example.com"; phone = "13800000001" } -NoAuth
$hasPwd = $null -ne $r.Body.data.PSObject.Properties["passWord"]
Check "注册 user1" { $r.Body.code -eq 200 -and $r.Body.data.id -gt 0 -and -not $hasPwd } ("code=$($r.Body.code), id=$($r.Body.data.id), 含passWord=$hasPwd") "code=200，id 有值，无 passWord"

$r = Invoke-Api -Method POST -Path "/users/register" -Body @{ userName = "user1"; passWord = "123456"; nickName = "用户1" } -NoAuth
Check "重复注册 user1" { $r.Body.code -ne 200 -and $r.Body.message -eq "用户名已存在" } ("message=$($r.Body.message)") "用户名已存在"

$r = Invoke-Api -Method POST -Path "/users/login" -Body @{ userName = "demo"; passWord = "123456" } -NoAuth
$hasPwd = $null -ne $r.Body.data.user.PSObject.Properties["passWord"]
$script:token = $r.Body.data.token
Check "登录 demo/123456 返回 token + user" { $r.Body.code -eq 200 -and $r.Body.data.user.userName -eq "demo" -and -not $hasPwd -and $r.Body.data.token.Length -gt 20 } ("code=$($r.Body.code), userName=$($r.Body.data.user.userName), 含passWord=$hasPwd, token长度=$($r.Body.data.token.Length)") "code=200，含 token，无 passWord"

$r = Invoke-Api -Method POST -Path "/users/login" -Body @{ userName = "demo"; passWord = "000000" } -NoAuth
Check "登录错误密码" { $r.Body.code -ne 200 -and $r.Body.message -eq "用户名或密码错误" } ("message=$($r.Body.message)") "用户名或密码错误"

Write-Host "`n== 5.3 简历模块（CRUD）==" -ForegroundColor Cyan
$resumeBody = @{
    userId = 1; realName = "张三"; gender = 1; phone = "13800138001"; email = "zhangsan@example.com"
    city = "杭州"; education = "本科"; experience = "3年"; expectedSalary = "15K-25K"
    skills = "Java,Spring Boot,MySQL"; introduction = "热爱编程，有良好的团队协作能力"
}
$r = Invoke-Api -Method POST -Path "/resumes" -Body $resumeBody
$rid = $r.Body.data.id
Check "① 创建简历" { $r.Body.code -eq 200 -and $rid -gt 0 -and $null -ne $r.Body.data.createTime } ("id=$rid, createTime=$($r.Body.data.createTime)") "创建成功，自动补时间"

$r = Invoke-Api -Method GET -Path "/resumes/$rid"
Check "② 简历详情" { $r.Body.code -eq 200 -and $r.Body.data.realName -eq "张三" } ("realName=$($r.Body.data.realName)") "返回完整简历"

$r = Invoke-Api -Method PUT -Path "/resumes/$rid" -Body @{ realName = "张三（已更新）"; phone = "13900139001"; expectedSalary = "20K-30K" }
Check "③ 部分更新（只传 3 个字段）" {
    $r.Body.code -eq 200 -and $r.Body.data.realName -eq "张三（已更新）" -and
    $r.Body.data.phone -eq "13900139001" -and $r.Body.data.email -eq "zhangsan@example.com" -and
    $r.Body.data.skills -eq "Java,Spring Boot,MySQL"
} ("realName={0}, email={1} 保留, skills={2} 保留" -f $r.Body.data.realName, $r.Body.data.email, $r.Body.data.skills) "其余字段原值保留"

$r = Invoke-Api -Method GET -Path "/resumes/list?userId=1"
Check "④ 用户简历列表" { $r.Body.code -eq 200 -and $r.Body.data.Count -eq 1 } ("{0} 条" -f $r.Body.data.Count) "1 条"

$r = Invoke-Api -Method DELETE -Path "/resumes/$rid"
Check "⑤ 删除简历" { $r.Body.code -eq 200 -and $r.Body.message -eq "删除成功" } ("message=$($r.Body.message)") "删除成功"

$r = Invoke-Api -Method GET -Path "/resumes/$rid"
Check "⑥ 删除后再查" { $r.Body.code -ne 200 -and $r.Body.message -eq "简历不存在" } ("message=$($r.Body.message)") "简历不存在"

Write-Host "`n== 5.4 收藏模块 ==" -ForegroundColor Cyan
$r = Invoke-Api -Method POST -Path "/favorites/add" -Body @{ userId = 1; jobId = 1 }
Check "添加收藏 {1,1}" { $r.Body.code -eq 200 -and $r.Body.message -eq "收藏成功" } ("message=$($r.Body.message)") "收藏成功"

$r = Invoke-Api -Method POST -Path "/favorites/add" -Body @{ userId = 1; jobId = 1 }
Check "重复收藏" { $r.Body.code -ne 200 -and $r.Body.message -eq "已收藏" } ("message=$($r.Body.message)") "已收藏"

$r = Invoke-Api -Method GET -Path "/favorites/list?userId=1"
Check "收藏列表返回 Job" { $r.Body.code -eq 200 -and $r.Body.data.Count -eq 1 -and $null -ne $r.Body.data[0].jobTitle } ("{0} 条, jobTitle={1}" -f $r.Body.data.Count, $r.Body.data[0].jobTitle) "1 条 Job"

$r = Invoke-Api -Method DELETE -Path "/favorites/remove?userId=1&jobId=1"
Check "取消收藏" { $r.Body.code -eq 200 -and $r.Body.message -eq "取消收藏成功" } ("message=$($r.Body.message)") "取消收藏成功"

$r = Invoke-Api -Method POST -Path "/favorites/add" -Body @{ userId = 1; jobId = 99 }
Check "收藏不存在的职位 99" { $r.Body.code -ne 200 -and $r.Body.message -eq "用户或职位不存在" } ("message=$($r.Body.message)") "用户或职位不存在"

Write-Host "`n== 5.5 投递模块 ==" -ForegroundColor Cyan
$r = Invoke-Api -Method POST -Path "/applications/apply" -Body @{ userId = 1; jobId = 2 }
Check "投递 {1,2}" { $r.Body.code -eq 200 -and $r.Body.message -eq "投递成功" } ("message=$($r.Body.message)") "投递成功"

$r = Invoke-Api -Method POST -Path "/applications/apply" -Body @{ userId = 1; jobId = 2 }
Check "重复投递" { $r.Body.code -ne 200 -and $r.Body.message -eq "已投递" } ("message=$($r.Body.message)") "已投递"

$r = Invoke-Api -Method GET -Path "/applications/list?userId=1"
$item = $r.Body.data[0]
Check "投递列表字段齐全" {
    $r.Body.code -eq 200 -and $r.Body.data.Count -eq 1 -and
    $null -ne $item.jobId -and $null -ne $item.jobTitle -and $null -ne $item.companyName -and
    $item.status -eq 0 -and $null -ne $item.applyTime
} ("jobId={0}, jobTitle={1}, status={2}" -f $item.jobId, $item.jobTitle, $item.status) "含 jobId/jobTitle/companyName/status/applyTime"

$r = Invoke-Api -Method PUT -Path "/applications/cancel?userId=1&jobId=2"
Check "取消投递（只改状态，不删记录）" {
    $r.Body.code -eq 200 -and $r.Body.message -eq "取消投递成功" -and
    (Invoke-Api -Method GET -Path "/applications/list?userId=1").Body.data.Count -eq 1
} ("message=$($r.Body.message), 记录仍在") "取消投递成功"

$r = Invoke-Api -Method POST -Path "/applications/apply" -Body @{ userId = 1; jobId = 2 }
Check "取消后再投递" { $r.Body.code -eq 200 -and $r.Body.message -eq "重新投递成功" } ("message=$($r.Body.message)") "重新投递成功"

$r = Invoke-Api -Method GET -Path "/jobs/2"
Check "职位2 applyCount" { $r.Body.data.applyCount -ge 1 } ("applyCount=$($r.Body.data.applyCount)") "≥ 1"

Write-Host "`n== 六、提交要求的 3 个接口 ==" -ForegroundColor Cyan
$r = Invoke-Api -Method GET -Path "/jobs/list?city=%E6%9D%AD%E5%B7%9E&keyword=Java"
Check "提交① 职位双条件筛选" { $r.Body.code -eq 200 -and $r.Body.data.Count -eq 1 -and $r.Body.data[0].companyName -eq "阿里巴巴" } ("{0} 条, {1} / {2}" -f $r.Body.data.Count, $r.Body.data[0].companyName, $r.Body.data[0].jobTitle) "1 条 阿里巴巴/Java"

$r = Invoke-Api -Method POST -Path "/users/login" -Body @{ userName = "demo"; passWord = "123456" } -NoAuth
$hasPwd = $null -ne $r.Body.data.user.PSObject.Properties["passWord"]
Check "提交② 预置账号登录" { $r.Body.code -eq 200 -and $r.Body.data.user.userName -eq "demo" -and -not $hasPwd -and $r.Body.data.token } ("code=$($r.Body.code), userName=$($r.Body.data.user.userName), 无passWord=$(-not $hasPwd), 有token=$([bool]$r.Body.data.token)") "code=200，含 token，无 passWord"

$r = Invoke-Api -Method POST -Path "/favorites/add" -Body @{ userId = 1; jobId = 3 }
if ($r.Body.message -eq "已收藏") {
    Invoke-Api -Method DELETE -Path "/favorites/remove?userId=1&jobId=3" | Out-Null
    $r = Invoke-Api -Method POST -Path "/favorites/add" -Body @{ userId = 1; jobId = 3 }
}
Check "提交③ 收藏字节职位 jobId=3" { $r.Body.code -eq 200 -and $r.Body.message -match "收藏成功" } ("code=$($r.Body.code), message=$($r.Body.message)") "收藏成功"

$r = Invoke-Api -Method GET -Path "/favorites/list?userId=1"
$hit = @($r.Body.data | Where-Object { $_.companyName -eq "字节跳动" }).Count
Check "收藏列表含字节跳动" { $r.Body.data.Count -ge 1 -and $hit -ge 1 } ("{0} 条，命中字节跳动 {1} 条" -f $r.Body.data.Count, $hit) "含 companyName=字节跳动"

Write-Host "`n== 第06步：JWT 认证 ==" -ForegroundColor Cyan
$saved = $script:token
$script:token = $null

$r = Invoke-Api -Method GET -Path "/users/info" -NoAuth
Check "① 不带 Token 访问 /users/info" { $r.HttpStatus -eq 401 -and $r.Body.code -eq 401 -and $r.Body.message -eq "请先登录" } ("HTTP $($r.HttpStatus), code=$($r.Body.code), message=$($r.Body.message)") "HTTP 401 + 请先登录"

$r = Invoke-Api -Method GET -Path "/users/info" -NoAuth
$script:token = "abc.def.ghi"
$r = Invoke-Api -Method GET -Path "/users/info"
Check "② 伪造 Token 被拒绝" { $r.HttpStatus -eq 401 -and $r.Body.code -eq 401 } ("HTTP $($r.HttpStatus), message=$($r.Body.message)") "HTTP 401"

$script:token = $saved
$r = Invoke-Api -Method GET -Path "/users/info"
$hasPwd = $null -ne $r.Body.data.PSObject.Properties["passWord"]
Check "③ 带 Token 访问 /users/info 返回当前用户" {
    $r.Body.code -eq 200 -and $r.Body.data.id -eq 1 -and $r.Body.data.userName -eq "demo" -and -not $hasPwd
} ("code=$($r.Body.code), id=$($r.Body.data.id), userName=$($r.Body.data.userName), 含passWord=$hasPwd") "demo 用户，无密码"

$script:token = $null
$r = Invoke-Api -Method GET -Path "/jobs/list" -NoAuth
Check "④ 白名单 /jobs/** 不带 Token 可用" { $r.Body.code -eq 200 -and $r.Body.data.Count -eq 3 } ("code=$($r.Body.code), {0} 条" -f $r.Body.data.Count) "code=200，3 条"

$r = Invoke-Api -Method GET -Path "/swagger-ui/index.html" -NoAuth
if ($r.HttpStatus -ne 200) {
    # SpringDoc 首次生成文档较慢，失败时再试一次
    Write-Host ("  (Swagger 首次请求失败：HTTP {0}, {1}，重试)" -f $r.HttpStatus, $r.Error) -ForegroundColor DarkGray
    Start-Sleep -Seconds 3
    $r = Invoke-Api -Method GET -Path "/swagger-ui/index.html" -NoAuth
}
Check "⑤ Swagger UI 可访问（白名单）" { $r.HttpStatus -eq 200 } ("HTTP $($r.HttpStatus) $($r.Error)") "HTTP 200"

$r = Invoke-Api -Method GET -Path "/v3/api-docs" -NoAuth
$schemeNames = @($r.Body.components.securitySchemes.PSObject.Properties.Name)
Check "⑥ OpenAPI 文档已配置 Bearer Token" { $r.HttpStatus -eq 200 -and $schemeNames -contains "Bearer Token" } ("schemes=$($schemeNames -join ',')") "含 Bearer Token"

$script:token = $saved

Write-Host ("`n===== 合计：PASS {0} / FAIL {1} =====" -f $script:pass, $script:fail) -ForegroundColor Yellow
$script:steps | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 "tools\verify-result.json"
Write-Host "明细已写入 tools\verify-result.json"
if ($script:fail -gt 0) { exit 1 }
