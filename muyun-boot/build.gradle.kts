plugins {
    java
    checkstyle
    alias(libs.plugins.quarkus)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(tasks.named("compileQuarkusGeneratedSourcesJava"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("compileQuarkusGeneratedSourcesJava"))
}

tasks.named("quarkusDependenciesBuild") {
    mustRunAfter(tasks.named("jandex"))
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.platform.bom))
    implementation("io.quarkus:quarkus-config-yaml")

    implementation(project(":muyun-core"))
    implementation(project(":muyun-database-std"))
    implementation(project(":muyun-platform"))

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")

    testImplementation("io.quarkus:quarkus-agroal")
    testImplementation("io.quarkus:quarkus-jdbc-postgresql")
    testImplementation(libs.testcontainers.postgresql)
}
