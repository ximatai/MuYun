plugins {
    id("org.kordamp.gradle.jandex")
}

tasks.named<Checkstyle>("checkstyleMain") {
    dependsOn(tasks.named("jandex"))
}
