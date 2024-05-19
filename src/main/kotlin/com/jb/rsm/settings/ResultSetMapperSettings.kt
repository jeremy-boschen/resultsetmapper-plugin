package com.jb.rsm.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.ThreeState
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.FROMROW_MAPPER_TEMPLATE
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSETMAPPER_FROMROW_METHOD
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSET_MAPPER_TEMPLATE
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSET_WRAPPER_TEMPLATE

enum class OverwriteStrategy(val description: String) {
    Always("Always overwrite existing code"),
    Never("Never overwrite existing code"),
    CommentOut("Comment out existing code"),
    OnlyIfGenerated("Overwrite existing code if original @Generated annotation is present")
}


class CodeTemplate(
    var templateName: String,
    var generate: Boolean,
    var overwriteStrategy: OverwriteStrategy,
    val overwriteStrategies: List<OverwriteStrategy>,
) {
    // Required for XML deserialization
    @Suppress("unused", "RemoveEmptySecondaryConstructorBody")
    constructor() : this("", false, OverwriteStrategy.Never, emptyList()) {
    }
}


@Service(Service.Level.PROJECT)
@State(
    name = "ResultSetMapperSettings",
    storages = [
        Storage(
            value = "resultsetmapper-plugin.xml",
            roamingType = RoamingType.DISABLED,
            useSaveThreshold = ThreeState.NO
        )],
    externalStorageOnly = true
)
class ResultSetMapperSettings(val project: Project) : PersistentStateComponent<ResultSetMapperSettings.State> {
    private var settings = State()

    override fun loadState(state: State) {
        this.settings.load(state)
    }

    // Used by the persistence framework
    override fun getState(): State {
        return settings
    }

    override fun noStateLoaded() {
        // Reset state
        settings = State()
    }

    class State {
        var packageName: String = "com.jb.jdbc"

        // A filter for which classes can be selected as DTO's for fromRow
        var fromRowClassFilter: String = "*"

        @Suppress("EnumValuesSoftDeprecate")
        var templates = listOf(
            CodeTemplate(
                RESULTSET_WRAPPER_TEMPLATE,
                true,
                OverwriteStrategy.Always,
                listOf(
                    OverwriteStrategy.Always,
                    OverwriteStrategy.Never,
                    OverwriteStrategy.OnlyIfGenerated
                )
            ),
            CodeTemplate(
                RESULTSET_MAPPER_TEMPLATE,
                true,
                OverwriteStrategy.Always,
                listOf(
                    OverwriteStrategy.Always,
                    OverwriteStrategy.Never,
                    OverwriteStrategy.OnlyIfGenerated
                ),
            ),
            CodeTemplate(
                FROMROW_MAPPER_TEMPLATE,
                true,
                OverwriteStrategy.Always,
                listOf(
                    OverwriteStrategy.Always,
                    OverwriteStrategy.Never,
                    OverwriteStrategy.OnlyIfGenerated
                )
            ),
            CodeTemplate(
                RESULTSETMAPPER_FROMROW_METHOD,
                true,
                OverwriteStrategy.OnlyIfGenerated,
                OverwriteStrategy.values().toList()
            )
        )

        fun getTemplateNamed(name: String): CodeTemplate? {
            return templates.firstOrNull {
                it.templateName == name
            }
        }

        fun load(state: State): State {
            this.packageName = state.packageName

            // Copy only the values which are exportable
            this.templates.forEach { template ->
                state.templates.firstOrNull { other ->
                    template.templateName == other.templateName
                }?.let { other ->
                    template.generate = other.generate
                    template.overwriteStrategy = other.overwriteStrategy
                }
            }

            return this
        }
    }
}

fun getResultMapperSettings(project: Project): ResultSetMapperSettings = project.service<ResultSetMapperSettings>()