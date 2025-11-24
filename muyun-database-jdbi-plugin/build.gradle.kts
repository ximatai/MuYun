plugins {
    java
    `java-library`
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))
    api(libs.muyun.database.jdbi)

    api(libs.yasson)
    api("io.quarkus:quarkus-jdbc-postgresql")
}
