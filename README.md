# 智能库存台账管理系统

面向仓储场景的前后端分离库存台账系统。项目提供登录鉴权、角色权限、商品与库存管理、出入库流水、按月分表、Excel 导入导出、条码扫描、图片管理和经营统计能力。

> 本仓库仅提交可复现的项目源码、配置模板与项目说明；不提交账号、密码、运行日志、上传文件、依赖目录或构建产物。

## 技术栈

- 后端：Java 8、Spring Boot 2.5、MyBatis-Plus、MySQL 8、Redis、JWT、AOP、EasyExcel、ZXing
- 前端：Vue 2、Vue CLI、Element UI、Axios、ECharts
- 部署：Nginx 反向代理、Linux、Maven、npm

## 我的实现内容

- 基于 JWT 与 Redis 实现登录态管理；拦截器统一校验请求并写入当前用户上下文。
- 完成用户、角色、菜单/权限、部门等基础管理，以及商品主数据、库存阈值和逻辑删除。
- 实现库存入库、出库和流水快照；库存变更同时更新当前库存与历史台账，避免主数据修改影响历史记录。
- 使用按月分表保存库存流水与登录日志，并在写入前校验动态表名、按需建表。
- 实现 Excel 模板导入导出、CODE_128 条码生成、扫码查询、商品多图上传与预览。
- 实现统计页面和请求/服务耗时日志；日志参数在输出前进行脱敏。

## 仓库结构

```text
my_springBoot_project-01/  Spring Boot API
vueproject-master/         Vue management console
docs/       架构、运行和面试说明
```

## 本地运行

1. 准备 MySQL 8、Redis 和数据库结构；数据库连接、Redis 地址、上传与日志目录均由环境变量提供。
2. 后端环境变量示例：

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/inventory_ledger?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='your-local-password'
$env:REDIS_HOST='127.0.0.1'
$env:REDIS_PORT='6379'
```

3. 启动后端：`mvn clean package -DskipTests`，随后执行生成的 jar。
4. 启动前端：`npm install` 后执行 `npm run dev`。

详细接口和部署说明见 `docs/`。
