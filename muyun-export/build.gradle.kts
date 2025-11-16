plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-core"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-jackson")
}
