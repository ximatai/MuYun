pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "MuYun"

include("muyun-core")
include("muyun-runtime-session")
include("muyun-runtime-gateway")
//include("muyun-core-uni")
//include("muyun-database")
include("muyun-database-jdbi-plugin")
//include("muyun-database-uni")
include("muyun-platform")
include("muyun-authorization")
include("muyun-proxy")
include("muyun-log")
include("muyun-fileserver")

include("muyun-export")
include("muyun-export-excel")

include("muyun-boot")
