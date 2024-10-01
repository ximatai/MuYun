plugins {
    java
    `java-library`
    checkstyle
    `configure-jandex`
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))
    api(project(":muyun-database"))
    api(project(":muyun-database-std"))

    api("io.quarkus:quarkus-hibernate-reactive-panache")
    api("io.quarkus:quarkus-reactive-pg-client")
}
