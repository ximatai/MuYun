plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-core"))
    api(project(":muyun-database-std"))
    api(project(":muyun-authorization"))

    api("io.quarkus:quarkus-scheduler")

    api(libs.easyCaptcha)
//    api(project(":muyun-database-uni"))
}
