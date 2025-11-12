plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Coroutines (only core, no Android dependencies)
    implementation(libs.kotlinx.coroutines.core)
    // JSR-330 Dependency Injection annotations (standard Java specification, platform-agnostic)
    implementation(libs.javax.inject)
}



