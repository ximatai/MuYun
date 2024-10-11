plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-core"))
    api(project(":muyun-database-std"))
    api(project(":muyun-authorization"))

    api(libs.commons.codes)
    api(libs.easyCaptcha)
//    api(project(":muyun-database-uni"))
}
