import io.deepmedia.tools.deployer.DeployerExtension

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.nextftc.publishing)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dokka)
}

allprojects {
    version = property("version") as String
    group = "dev.nextftc"
}

subprojects {
    extensions.configure<DeployerExtension> {
        projectInfo {
            url = "https://nextftc.dev/"
            scm {
                fromGithub("NextFTC", "control")
            }
            license("BSD 3-Clause License", "https://opensource.org/license/bsd-3-clause")
            developer("Zach Harel", "ftc@zharel.me", url = "https://github.com/zachwaffle4")
            developer("Davis Luxenberg", "davis.luxenberg@outlook.com", url = "https://github.com/BeepBot99")
            developer("Rowan McAlpin", "rowan@nextftc.dev", url = "https://rowanmcalpin.com")
        }
    }

    dokka {
        pluginsConfiguration.html {
            footerMessage.set("Copyright © 2026 NextFTC - Licensed under the BSD-3-Clause license.")
        }
    }

    dependencies {
        dokkaPlugin("org.jetbrains.dokka:mathjax-plugin")
    }
}

dependencies {
    dokka(project(":units"))
    dokka(project(":linalg"))
    dokka(project(":control2"))
    dokkaPlugin("org.jetbrains.dokka:mathjax-plugin")
}