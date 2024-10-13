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
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.salt.ui.desktop)
    implementation(libs.okhttp)
    implementation(libs.multiplatform.setting)

}
apply(from = "wix.gradle.kts")
compose.desktop {
    application {
        mainClass = "com.lonx.ECJTUAutoLogin"
        jvmArgs += listOf("-Dfile.encoding=GBK")
        nativeDistributions {
            targetFormats(TargetFormat.Msi,TargetFormat.Exe)
            packageName = "ECJTULoginTool"
            packageVersion = "1.0.1"
            description = "华交校园网工具"
            windows{
                shortcut=true
                perUserInstall=false
                upgradeUuid="13b76f0a-b6be-4ca8-a50b-d7a9732c63ff"
            }
        }
    }
}
