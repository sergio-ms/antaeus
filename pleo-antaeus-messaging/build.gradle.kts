plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation("com.rabbitmq:amqp-client:5.9.0")
    implementation(project(":pleo-antaeus-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}
