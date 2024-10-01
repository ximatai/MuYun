plugins {
    java
    `java-library`
    checkstyle
}

dependencies {
    api(libs.jdbi.core)
//    api(libs.jdbi.postgres)
}

