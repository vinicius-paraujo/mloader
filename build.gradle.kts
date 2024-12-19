plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.markineo"
version = "1.0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "codemc"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        name = "citizens-repo"
        url = uri("https://maven.citizensnpcs.co/repo")
    }
}

dependencies {
    compileOnly(files("src/main/resources/ExecutableBlocks.jar"))
    compileOnly(files("src/main/resources/SCore.jar"))
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.14.0")
    compileOnly("net.citizensnpcs:citizens-main:2.0.35-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("mysql:mysql-connector-java:8.0.32")
}

tasks.shadowJar {
    archiveClassifier.set("")

    // Exclui os arquivos JAR externos para evitar conflitos
    exclude("ExecutableBlocks.jar")
    exclude("SCore.jar")

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}