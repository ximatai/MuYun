plugins {
    java
    checkstyle
    signing
    id("io.github.jeadyx.sonatype-uploader") version "2.8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    apply {
        plugin("java")
        plugin("checkstyle")
        plugin("configure-jandex")
        plugin("maven-publish")
        plugin("signing")
        plugin("io.github.jeadyx.sonatype-uploader")
    }

    group = "net.ximatai.muyun"
    version = "0.1.0"

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
//            groupId = "net.ximatai.muyun"
//            artifactId = "muyun"
//            version = "0.1.0"
                from(components["java"])
                pom {
//                name = "MuYun"
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
            maven {
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }
    }


    sonatypeUploader {
        repositoryPath = layout.buildDirectory.dir("repo").get().asFile.path
        tokenName = findProperty("sonatype.token").toString()
        tokenPasswd = findProperty("sonatype.password").toString()
//    tokenName = System.getenv("SONATYPE_USERNAME")
//    tokenPasswd = System.getenv("SONATYPE_PASSWORD")
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

    tasks.named<Javadoc>("javadoc") {
        mustRunAfter(tasks.named("jandex"))
    }

    tasks.withType<GenerateModuleMetadata> {
        suppressedValidationErrors.add("enforced-platform")
    }

    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
        mavenLocal()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<Test> {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        maxHeapSize = "2g"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
}

