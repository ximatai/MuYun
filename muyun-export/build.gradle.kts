plugins {
    id("java")
}

group = "net.ximatai.muyun"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":muyun-core"))
    implementation("io.quarkus:quarkus-arc")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

