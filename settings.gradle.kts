pluginManagement {
    repositories {
//        maven { url = uri("https://mirrors.cloud.tencent.com/repository/gradle-plugins/") }
//        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()  // 保留官方 Gradle 插件门户
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.5"
}

rootProject.name = "MuYun"

require(JavaVersion.current() >= JavaVersion.VERSION_21) {
    "You must use at least Java 21 to build the project, you're currently using ${System.getProperty("java.version")}"
}

include("muyun-core")
//include("muyun-core-uni")
include("muyun-database")
include("muyun-database-std")
//include("muyun-database-uni")
//include("muyun-msg")
include("muyun-platform")
include("muyun-authorization")
include("muyun-proxy")
include("muyun-log")

include("muyun-boot")

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        publishing.onlyIf { false }
    }
}
