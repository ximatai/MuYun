plugins {
    java
    `java-library`
}

dependencies {
    api(project(":muyun-export"))
    implementation(libs.poi.ooxml)
}
