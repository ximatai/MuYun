# MuYun 轻代码开发平台

[![Maven Central Version](https://img.shields.io/maven-central/v/net.ximatai.muyun/muyun-core)](https://central.sonatype.com/artifact/net.ximatai.muyun/muyun-core/overview)
[![ci](https://github.com/ximatai/MuYun/actions/workflows/ci.yml/badge.svg)](https://github.com/ximatai/MuYun/actions/workflows/ci.yml)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ximatai/MuYun)

## 特点

* 云原生
* 同步/异步双轨支持
* JVM系第一梯队性能
* 分层次构建，按需插拔（意味着可以按需依赖Jar包）
* 程序员友好（口号：“一切为了开发人员不加班”）
* 前后端分离（不仅仅是技术层面，从业务设计就是全面分离的，以后端优先为出发点）
* 测试驱动开发

## 架构设计

![CleanShot 2024-10-28 at 14 48 22@2x](https://github.com/user-attachments/assets/4cd95a86-8099-4df7-97a1-97fafbd3aed5)

[MuYun轻代码平台能力一览.pdf](https://github.com/ximatai/MuYun/blob/master/MuYun轻代码平台能力一览.pdf)

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
    - [x] 删除数据时对数据归档
    - [x] 通用业务字段自动创建
    - [x] 数据加密
    - [x] 数据校验
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
    - [x] 单据编码
    - [x] 通知公告
    - [x] 在线用户
    - [ ] 工作流
    - [ ] 站内信
* [ ] 微服务
    - [ ] 通信

### 前端

**暂无，目前精力都放在对后端的持续推进上**

## 开发指南

如果你是需要使用 MuYun 平台实现自己的项目，可以移步 https://github.com/ximatai/MuYunStarter ，
这是我们准备好的专门用来使用打包好的 MuYun Jar 包进行业务开发的起步项目。如果你想了解 MuYun 本身是怎么开发的可以继续往下看。

本项目使用 Java 21 开发。

本项目目前对 [PostgreSQL](https://www.postgresql.org/) 做了完整适配，其他数据库需要做轻微改造才能兼容。

可以使用 [Docker Compose](https://docs.docker.com/compose/) 启动数据库：

```shell
docker compose up -d
```

或者 docker 命令：

```shell
docker run --rm  -p 54324:5432 -e POSTGRES_PASSWORD=muyun2024 -e POSTGRES_DB=muyun  postgres:17-alpine
```

后端开发环境启动（首次启动会引导你设置管理员账号密码）：

```shell
./gradlew --console=plain :muyun-boot:quarkusDev
```
## 一些视频教程
* [你从未见过的 Java 快速开发框架](https://www.bilibili.com/video/BV1w6zGYkEPo)
* [给 Java 企业级开发来点不一样的](https://www.bilibili.com/video/BV1W2z8YhEqV)
* [软删除如果你不满意的话，那么数据归档呢？](https://www.bilibili.com/video/BV1DHiUYSEHD)
