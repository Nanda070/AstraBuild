plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("org.hidetake.ssh") version "2.11.2"
}

group = "com.astrabuild"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
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

apply(from = "deploy.gradle")