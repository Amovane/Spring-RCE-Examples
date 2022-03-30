plugins {
    kotlin("jvm") version "1.5.31"
    java
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.unboundid:unboundid-ldapsdk:3.1.1")
    implementation("org.apache.logging.log4j:log4j-api:2.8.2")
    implementation("org.apache.logging.log4j:log4j-core:2.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}