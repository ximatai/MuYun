plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-core"))
    api(libs.muyun.database)
}
