plugins {
    // Apply the java plugin to add support for Java
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs("lib")
    }
}

dependencies {
    implementation("io.vavr:vavr:0.10.0")
    implementation("io.vavr:vavr-match:0.10.0")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.apiguardian:apiguardian-api:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
