plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-messaging"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}