plugins {
    java
    `java-library`
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))
    api(project(":muyun-database"))

    api(libs.yasson)
    api("io.quarkus:quarkus-agroal")
    api("io.quarkus:quarkus-jdbc-postgresql")
}
