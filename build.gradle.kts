plugins {
    id("java")
    id("war")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "ru.yandex"
version = "1.0"
project.setProperty("archivesBaseName", "blog-app")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-webmvc:6.2.15")
    implementation("org.springframework.data:spring-data-jdbc:3.4.1")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.glassfish:jakarta.el:4.0.2")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.projectlombok:lombok:1.18.42")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework:spring-test:6.2.15")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}