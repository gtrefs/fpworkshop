plugins {
    // Apply the java plugin to add support for Java
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


repositories {
    flatDir {
        dirs("lib")
    }
}

dependencies {
    implementation("io.vavr:vavr:0.10.0")
    implementation("io.vavr:vavr-match:0.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.0")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.apiguardian:apiguardian-api:1.0.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.1.0")
    testRuntime("org.junit.platform:junit-platform-commons:1.1.0")
    testRuntime("org.junit.platform:junit-platform-engine:1.1.0")
    testRuntime("org.opentest4j:opentest4j:1.0.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
