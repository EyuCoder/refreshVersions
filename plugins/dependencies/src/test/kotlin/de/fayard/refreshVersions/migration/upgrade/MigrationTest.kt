package de.fayard.refreshVersions.migration.upgrade

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import java.io.File

class MigrationTest : StringSpec({
    val testResources: File = File(".").absoluteFile.resolve("src/test/resources")

    "Ignore lines that do not contain version" {
        val lines = """
            plugins {
                kotlin("jvm")
            }

            version = "2"
            group = "de.fayard"

            repositories {
                mavenCentral()
            }

        """.trimIndent()
        lines.lines().forAll { line -> replaceVersionWithUndercore(line) shouldBe null }
    }

    "Replace version with underscore" {
        val input = """
            val a = "1.3"
            val b = "1.2.3"
            implementation("com.example:name:1.2.3")
            implementation(group : "com.example" name: "name" version :"1.2.3")
            implementation('com.example:name:1.2.3')
            implementation("com.example:name:${'$'}exampleVersion")
            implementation("com.example:name:${'$'}version")
            implementation('com.example:name:${'$'}exampleVersion')
            implementation('com.example:name:${'$'}version')
            implementation('com.example:name:1.2.3-alpha1')
            implementation('com.example:name:1.2.3-alpha-1')
            implementation('com.example:name:1.2.3.alpha.1')
            implementation('com.example:name:1.2.3-beta-1')
            implementation('com.example:name:1.2.3.beta.1')
            implementation('com.example:name:1.2.3.beta1')
            implementation('com.example:name:1.2.3-eap-1')
            implementation('com.example:name:1.2.3.eap.1')
            implementation('com.example:name:1.2.3.eap1')
        """.trimIndent().lines()
        val expected = """
            val a = "_"
            val b = "_"
            implementation("com.example:name:_")
            implementation(group : "com.example" name: "name" version :"_")
            implementation('com.example:name:_')
            implementation("com.example:name:_")
            implementation("com.example:name:_")
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
        """.trimIndent().lines()
        input.size shouldBeExactly expected.size
        List(input.size) { input[it] to expected[it] }
            .forAll { (input, output) ->
                replaceVersionWithUndercore(input) shouldBe output
            }
    }

    "Search for files that may contain dependency notations" {
        val expected = """
            app/feature/build.gradle.kts
            build.gradle
            buildSrc/src/main/kotlin/Dependencies.kt
            buildSrc/src/main/kotlin/Deps.kt
            buildSrc/src/main/kotlin/Libs.kt
            buildSrc/src/main/kotlin/my/package/Deps.kt
            buildSrc/src/main/kotlin/Versions.kt
            deps.gradle
            gradle/dependencies.gradle
            gradle/libraries.gradle
            libraries.groovy
            libs.gradle
        """.trimIndent().lines()
        val dir = testResources.resolve("migration.files")
        findFilesWithDependencyNotations(dir).map { it.relativeTo(dir).path }.sorted() shouldBe expected.sorted()
    }
})


@Language("RegExp")
val underscoreRegex =
    "(['\":])(?:\\\$\\w+ersion|(?:\\d+\\.){1,2}\\d+)(?:[.-]?(?:alpha|beta|rc|eap)[-.]?\\d+)?([\"'])".toRegex()

fun replaceVersionWithUndercore(line: String): String? = when {
    underscoreRegex.containsMatchIn(line) -> line.replace(underscoreRegex, "\$1_\$2")
    else -> null
}

fun findFilesWithDependencyNotations(fromDir: File): List<File> {
    require(fromDir.isDirectory()) { "Expected a directory, got ${fromDir.absolutePath}" }
    val expectedNames = listOf("build", "build.gradle", "deps", "dependencies", "libs", "libraries", "versions")
    val expectedExtesions = listOf("gradle", "kts", "groovy", "kt")
    return fromDir.walkBottomUp()
        .filter {
            it.extension in expectedExtesions && it.nameWithoutExtension.toLowerCase() in expectedNames
        }
        .toList()
}
