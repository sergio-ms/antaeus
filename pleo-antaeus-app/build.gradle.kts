plugins {
    application
    kotlin("jvm")
}

kotlinProject()

dataLibs()

application {
    mainClassName = "io.pleo.antaeus.app.AntaeusApp"
}

dependencies {
    implementation(project(":pleo-antaeus-data"))
    implementation(project(":pleo-antaeus-rest"))
    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-models"))
    implementation(project(":pleo-antaeus-scheduling"))
    implementation(project(":pleo-antaeus-messaging"))
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}
