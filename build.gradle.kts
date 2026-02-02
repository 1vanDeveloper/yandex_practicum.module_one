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
    implementation("org.springframework:spring-webmvc:6.2.10")
    implementation("org.springframework.data:spring-data-jdbc:3.4.1")
    implementation("org.postgresql:postgresql:42.7.3")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}