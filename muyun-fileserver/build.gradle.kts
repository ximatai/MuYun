plugins {
    java
    `java-library`
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))
    api("io.quarkus:quarkus-rest")
    api("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-vertx")
    api("io.quarkus:quarkus-reactive-routes")
}
