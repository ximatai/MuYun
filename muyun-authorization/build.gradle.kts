plugins {
    java
    `java-library`
    checkstyle
    `configure-jandex`
}

dependencies {
    api(project(":muyun-core"))
    api(project(":muyun-database-std"))
}
