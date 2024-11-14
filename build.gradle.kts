plugins {
    java
    checkstyle
    signing
    id("io.github.jeadyx.sonatype-uploader") version "2.8"
    id("org.kordamp.gradle.jandex") version "2.1.0"
}

allprojects {
    group = "net.ximatai.muyun"
    version = "1.0.0-SNAPSHOT"
    version = "0.1.5"

    repositories {
        mavenLocal()
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("checkstyle")
        plugin("maven-publish")
        plugin("signing")
        plugin("io.github.jeadyx.sonatype-uploader")
        plugin("org.kordamp.gradle.jandex")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    description =
                        "A cloud-native, asynchronous, developer-first, frontend-backend decoupled, and plug-and-play light-code platform."
                    url = "https://github.com/ximatai/MuYun"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "aruis"
                            name = "Rui Liu"
                            email = "lovearuis@gmail.com"
                            organization = "戏码台"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/ximatai/MuYun.git"
                        developerConnection = "scm:git:ssh://github.com/ximatai/MuYun.git"
                        url = "https://github.com/ximatai/MuYun"
                    }
                }
            }
        }
        repositories {
//            maven {
//                url = uri(layout.buildDirectory.dir("repo"))
//            }

            maven {
                url = uri("http://192.168.3.19:8081/repository/maven-snapshots/")
                isAllowInsecureProtocol = true
                credentials {
                    username = findProperty("office.maven.username").toString()
                    password = findProperty("office.maven.password").toString()
                }
            }
        }
    }

    sonatypeUploader {
        repositoryPath = layout.buildDirectory.dir("repo").get().asFile.path
        tokenName = findProperty("sonatype.token").toString()
        tokenPasswd = findProperty("sonatype.password").toString()
    }

    signing {
        sign(publishing.publications["mavenJava"])
        useInMemoryPgpKeys(
            findProperty("signing.keyId").toString(),
            findProperty("signing.secretKey").toString(),
            findProperty("signing.password").toString()
        )
    }

    tasks.withType<Javadoc> {
        enabled = false
    }

    tasks.named<Checkstyle>("checkstyleMain") {
        dependsOn(tasks.named("jandex"))
    }

    tasks.named<Javadoc>("javadoc") {
        mustRunAfter(tasks.named("jandex"))
    }

    tasks.withType<GenerateModuleMetadata> {
        suppressedValidationErrors.add("enforced-platform")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        maxHeapSize = "2g"
    }

}

tasks.register("prepareRepo") {
    group = "release"
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "publishMavenJavaPublicationToMavenRepository" } })
}

tasks.register("releaseAllJars") {
    group = "release"
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "publishToSonatype" } })
}
