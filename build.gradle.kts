plugins {
    // Apply the java plugin to add support for Java
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs("lib")
    }
}

dependencies {
    testImplementation("io.vavr:vavr:0.10.0")
    testImplementation("io.vavr:vavr-match:0.10.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.apiguardian:apiguardian-api:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("net.jqwik:jqwik:1.8.3")
    testImplementation("org.testcontainers:kafka:1.19.7")
    testImplementation("org.apache.kafka:kafka-clients:3.5.1")
    testImplementation("net.jqwik:jqwik-testcontainers:0.5.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.jsoup:jsoup:1.17.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        includeEngines("jqwik", "junit-jupiter")
    }
}
