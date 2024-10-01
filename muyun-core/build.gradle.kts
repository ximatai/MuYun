plugins {
    java
    `java-library`
    checkstyle
    `configure-jandex`
    `maven-publish`
    signing
    id("io.github.jeadyx.sonatype-uploader") version "2.8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.ximatai.muyun"
            artifactId = "muyun"
            version = "0.1.0"
            from(components["java"])
            pom {
                name = "MuYun"
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

tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("enforced-platform")
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))

    api(project(":muyun-database"))

    api("io.quarkus:quarkus-rest")
    api("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-vertx")
    api("io.quarkus:quarkus-undertow")
    api("io.quarkus:quarkus-reactive-routes")
    api("io.quarkus:quarkus-scheduler")
    api("io.quarkus:quarkus-rest-jackson")
    api("io.quarkus:quarkus-smallrye-openapi")

    api(libs.bcprov)

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}
