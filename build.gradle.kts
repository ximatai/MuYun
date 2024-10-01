plugins {
    java
    checkstyle
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    apply { plugin("java") }

    group = "net.ximatai"
    version = "0.0.10"

    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
        mavenLocal()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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

