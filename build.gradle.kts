
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

//    implementation("io.ktor:ktor-server-core:2.3.0")
//    implementation("io.ktor:ktor-server-netty:2.3.0")
    // Логирование
//    implementation("ch.qos.logback:logback-classic:1.4.12")

    // ContentNegotiation для работы с JSON
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")

    // Работа с базой через Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    // Драйвер PostgreSQL
    implementation("org.postgresql:postgresql:42.7.2")

    // Поддержка конфигурации через YAML (возможно будет нужна)
//    implementation("io.ktor:ktor-server-config-yaml:2.3.0")

    // Тестирование базы данных
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.ktor:ktor-server-tests:2.3.13")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.0")
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")

    implementation("com.zaxxer:HikariCP:6.2.1") // Пул соединений HikariCP

    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
