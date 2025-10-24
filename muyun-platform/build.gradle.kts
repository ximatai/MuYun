plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-core"))
    api(libs.muyun.database)
    api(project(":muyun-authorization"))

    api(libs.easyCaptcha)
//    api(project(":muyun-database-uni"))
}
