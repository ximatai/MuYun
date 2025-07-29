plugins {
    java
    `java-library`
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))

    api(project(":muyun-util"))
    api(project(":muyun-database"))
    api(project(":muyun-fileserver"))

    api("io.quarkus:quarkus-rest")
    api("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-vertx")
    api("io.quarkus:quarkus-reactive-routes")
    api("io.quarkus:quarkus-rest-jackson")
    api("io.quarkus:quarkus-smallrye-openapi")
    api("io.quarkus:quarkus-scheduler")

    api(libs.commons.codes)
    api(libs.bcprov)
    api(libs.caffeine)

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}
