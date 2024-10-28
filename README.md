# MuYun 轻代码开发平台

[![Maven Central Version](https://img.shields.io/maven-central/v/net.ximatai.muyun/muyun-core)](https://central.sonatype.com/artifact/net.ximatai.muyun/muyun-core/overview)
[![ci](https://github.com/ximatai/MuYun/actions/workflows/ci.yml/badge.svg)](https://github.com/ximatai/MuYun/actions/workflows/ci.yml)

## 特点

* 云原生
* 同步/异步双轨支持
* JVM系第一梯队性能
* 分层次构建，按需插拔（意味着可以按需依赖Jar包）
* 程序员友好（口号：“一切为了开发人员不加班”）
* 前后端分离（不仅仅是技术层面，从业务设计就是全面分离的，以后端优先为出发点）
* 测试驱动开发

## 架构设计

![CleanShot 2024-10-28 at 14 41 30](https://github.com/user-attachments/assets/8fb32d60-3015-4643-89bb-5fb69e612c30)

## 开发进度

### 后端

* [x] 主体框架搭建
* [x] 同步数据库访问接入
* [ ] 异步数据库访问接入
* [x] 标准增删改查能力接入
  - [x] 数据新增
  - [x] 数据修改
  - [x] 数据删除
  - [x] 数据排序
  - [x] 数据查询
    - [x] 数据查询-单条
    - [x] 数据查询-多行-分页
    - [x] 数据查询-多行-分页-条件查询
    - [x] 数据查询-多表关联
    - [x] 数据查询-主子表关联
    - [x] 数据查询-树形构建
    - [x] 数据查询-数据脱敏
* [x] 扩展能力接入
    - [x] 代码内创建表
    - [x] 软删除
    - [x] 通用业务字段自动创建
    - [x] 数据加密
    - [x] 数据签名（数据完整性校验） 
    - [x] 内部数据变动广播
    - [x] 数据变动广播到SockJS
    - [x] 文件管理
    - [x] 内嵌反向代理服务器
* [x] 日志记录
    - [x] 操作日志
    - [x] 异常日志
    - [x] 登录日志
* [ ] UI渲染接口
    - [ ] 列表配置
    - [ ] 查询配置
    - [ ] 表单配置
* [ ] 平台业务
    - [x] 数据字典
    - [x] 机构
    - [x] 部门
    - [x] 人员
    - [x] 角色
    - [x] 模块
    - [x] 菜单
    - [x] 权限
      - [x] 功能权限
      - [x] 数据权限
    - [ ] 单据编码
    - [ ] 工作流
    - [x] 通知公告
    - [ ] 站内信
* [ ] 微服务
    - [ ] 通信

### 前端

**暂无，待后端开发到80%以上后再考虑推进前端开发**

## 开发指南

本项目使用 Java 21 开发。

本项目目前支持使用 [PostgreSQL](https://www.postgresql.org/) 作为数据库，未来会引入 [MySQL](https://www.mysql.com/) 支持。

可以使用 [Docker Compose](https://docs.docker.com/compose/) 一键启动环境：

```shell
docker compose up -d
```

后端开发环境启动：

```shell
./gradlew :muyun-boot:quarkusDev
```
