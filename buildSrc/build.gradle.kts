plugins {
    `kotlin-dsl`
    `java-library`
}

repositories {
    maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven") }
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("org.kordamp.gradle:jandex-gradle-plugin:2.0.0")
}
