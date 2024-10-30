pluginManagement {
    repositories {
//        maven { url = uri("https://mirrors.cloud.tencent.com/repository/gradle-plugins/") }
//        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()  // 保留官方 Gradle 插件门户
    }
}

rootProject.name = "MuYun"

include("muyun-core")
//include("muyun-core-uni")
include("muyun-database")
include("muyun-database-std")
//include("muyun-database-uni")
include("muyun-platform")
include("muyun-authorization")
include("muyun-proxy")
include("muyun-log")
include("muyun-fileserver")

include("muyun-boot")
