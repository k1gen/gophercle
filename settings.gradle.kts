buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath(kotlin("gradle-plugin", version = "1.9.0"))
    }
}

rootProject.name = "gophercle"
include(":app")
