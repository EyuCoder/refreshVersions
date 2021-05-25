package de.fayard.refreshVersions.core

import de.fayard.refreshVersions.core.internal.RefreshVersionsConfigHolder
import de.fayard.refreshVersions.core.internal.versions.VersionsPropertiesModel
import de.fayard.refreshVersions.core.internal.versions.readFrom
import de.fayard.refreshVersions.core.internal.versions.writeTo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class RefreshVersionsCleanupTask : DefaultTask() {

    @TaskAction
    fun cleanUpVersionsProperties() {
        val model = VersionsPropertiesModel.readFrom(RefreshVersionsConfigHolder.versionsPropertiesFile)

        val cleanupSections = model.sections.map { section ->
            when (section) {
                is VersionsPropertiesModel.Section.Comment -> section
                is VersionsPropertiesModel.Section.VersionEntry -> section.copy(availableUpdates = emptyList())
            }
        }
        model.copy(sections = cleanupSections).writeTo(RefreshVersionsConfigHolder.versionsPropertiesFile)
    }

    @TaskAction
    fun cleanUpSettings() {
        val settingsFiles = listOf("settings.gradle", "settings.gradle.kts", "buildSrc/settings.gradle", "buildSrc/settings.gradle.kts")
            .mapNotNull { project.file(it).takeIf { it.exists() } }
        settingsFiles.forEach { settings ->
            val oldContent = settings.readLines()
            val newContent = oldContent.filterNot { it.contains("////") && it.contains("available") }
            val newLineAtTheEnd = if (newContent.lastOrNull().isNullOrBlank()) "" else "\n"
            if (newContent.size != oldContent.size) {
                settings.writeText(newContent.joinToString(separator = "\n", postfix = newLineAtTheEnd))
            }
        }
    }
}
