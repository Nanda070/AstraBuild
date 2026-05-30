plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = "com.astrabuild"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    // compileOnly(project(":AstraSMP")) // Раскомментировать при настройке монорепозитория
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    javadoc {
        options.encoding = "UTF-8"
    }
}