plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-core"))
    //TODO 测试同时依赖的情况
    api(project(":muyun-database-std"))
    api(project(":muyun-authorization"))

    api(libs.commons.codes)
    api(libs.easyCaptcha)
    api(libs.caffeine)
//    api(project(":muyun-database-uni"))
}
