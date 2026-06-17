plugins {
    java
}

group = findProperty("pluginGroup") as String? ?: "com.sandustnetwork"

val pluginVersionProperty = findProperty("pluginVersion") as String?
version = pluginVersionProperty ?: "1.0.0-SNAPSHOT"

val mcVersion = findProperty("mcVersion") as String? ?: findProperty("defaultMcVersion") as String? ?: "1.21.4"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveBaseName.set("FineClaim")
    archiveClassifier.set("mc$mcVersion")
    archiveExtension.set("jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register("printVersion") {
    group = "help"
    description = "Prints the resolved plugin version"
    doLast {
        println(project.version)
    }
}

tasks.register("printMcVersion") {
    group = "help"
    description = "Prints the resolved Minecraft version"
    doLast {
        println(mcVersion)
    }
}
