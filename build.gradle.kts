
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
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.kotlinx.serialization.json)

//    testImplementation(libs.ktor.server.test.host)
//    testImplementation(libs.kotlin.test.junit)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // ContentNegotiation для работы с JSON
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")

    // Работа с базой через Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.59.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.59.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.59.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.59.0")

    // Драйвер PostgreSQL
    implementation("org.postgresql:postgresql:42.7.2")

    // Поддержка конфигурации через YAML (возможно будет нужна)
//    implementation("io.ktor:ktor-server-config-yaml:2.3.0")

    // Kotlin и JUnit 5
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    // Ktor test
    testImplementation("io.ktor:ktor-server-tests:2.3.13")
    testImplementation("io.ktor:ktor-server-test-host:2.3.13")
// H2 для in-memory тестов
    testImplementation("com.h2database:h2:2.2.224")

    implementation("com.zaxxer:HikariCP:6.2.1") // Пул соединений HikariCP

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.mindrot:jbcrypt:0.4")
}
