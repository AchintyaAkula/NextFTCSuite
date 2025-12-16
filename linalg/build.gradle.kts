plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spotless)
}

description = "A custom linear algebra library for NextControl."

dependencies {
    implementation(libs.ejml)

    testImplementation(libs.bundles.kotest)
}

nextFTCPublishing {
    displayName = "NextControl Linear Algebra"
    logoPath = "../assets/logo-icon.svg"
}

kotlin {
    jvmToolchain(8)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjvm-default=all", "-Xconsistent-data-class-copy-visibility")
    }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

spotless {
    kotlin {
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_code_style" to "intellij_idea",
                "indent_size" to "4",
                "continuation_indent_size" to "4",
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "max_line_length" to "100",
            ),
        )
    }
    kotlinGradle {
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_code_style" to "intellij_idea",
                "indent_size" to "4",
                "continuation_indent_size" to "4",
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "max_line_length" to "100",
            ),
        )
    }
}
