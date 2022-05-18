plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.21"
}

kotlinProject()

dependencies {
    implementation("com.rabbitmq:amqp-client:5.14.2")
    implementation(project(":pleo-antaeus-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}
