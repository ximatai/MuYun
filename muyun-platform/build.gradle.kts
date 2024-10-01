plugins {
    java
    `java-library`
    checkstyle
    `configure-jandex`
}

dependencies {
    api(project(":muyun-core"))
    //TODO 测试同时依赖的情况
    api(project(":muyun-database-std"))
    api(project(":muyun-authorization"))
//    api(project(":muyun-database-uni"))
}
