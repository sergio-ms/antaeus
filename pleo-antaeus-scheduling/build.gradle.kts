plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-messaging"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}