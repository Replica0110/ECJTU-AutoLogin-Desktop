import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.lonx"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.github.moriafly:salt-ui-desktop:2.2.0-dev08")
    implementation("com.russhwolf:multiplatform-settings:1.2.0")

}
apply(from = "wix.gradle.kts")
compose.desktop {
    application {
        mainClass = "com.lonx.LoginMain"
        nativeDistributions {
            targetFormats(TargetFormat.Msi,TargetFormat.Exe)
            packageName = "ECJTULoginTool"
            packageVersion = "1.0.1"
            description = "华东交通大学校园网登录工具"
            vendor = "Agines02"
            copyright = "Copyright 2023 Agines02."
            windows{
                shortcut=true
                perUserInstall=false
                upgradeUuid="13b76f0a-b6be-4ca8-a50b-d7a9732c63ff"
            }
        }
    }
}
