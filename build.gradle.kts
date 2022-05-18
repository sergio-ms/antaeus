import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    //kotlin("jvm") version "1.3.70" apply false
    kotlin("jvm") version "1.6.21" //apply false
    //kotlin("plugin.serialization") version "1.6.21"
}

allprojects {
    group = "io.pleo"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "11"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

