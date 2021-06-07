@file:Suppress("PackageDirectoryMismatch", "SpellCheckingInspection", "unused")

import de.fayard.refreshVersions.core.internal.ArtifactVersionKeyRule
import de.fayard.refreshVersions.core.internal.DependencyGroup
import org.gradle.api.Incubating

/**
 * painless Kotlin dependency injection
 *
 * - [Official website here](https://kodein.org/di/)
 * - GitHub page: [Kodein-Framework/Kodein-DI](https://github.com/Kodein-Framework/Kodein-DI)
 * - [GitHub Releases here](https://github.com/Kodein-Framework/Kodein-DI/releases)
 */

@Incubating
object Kodein {

    val bom: String
        get() = "org.kodein.di:kodein-bom:_"
            .also { di.usePlatformConstraints = true }

    val di = DI(usePlatformConstraints = false)

    class DI(usePlatformConstraints: Boolean) : DependencyGroup(
        group = "org.kodein.di",
        rule = ArtifactVersionKeyRule(
            artifactPattern = "  org.kodein.di:kodein-di(-*)",
            versionKeyPattern = "    ^^^^^^^^^              "
        ),
        usePlatformConstraints = usePlatformConstraints
    ) {
        val androidCore
            get() = module("kodein-di-framework-android-core")
        val androidSupport
            get() = module("kodein-di-framework-android-support")
        val androidx
            get() = module("kodein-di-framework-android-x")
        val configurableJS
            get() = module("kodein-di-conf-js")
        val configurableJvm
            get() = module("kodein-di-conf-jvm")
        val js
            get() = module("kodein-di-js")
        val jsr330
            get() = module("kodein-di-jxinject-jvm")
        val ktor
            get() = module("kodein-di-framework-ktor-server-jvm")
        val tornadofx
            get() = module("kodein-di-framework-tornadofx-jvm")
    }
}


