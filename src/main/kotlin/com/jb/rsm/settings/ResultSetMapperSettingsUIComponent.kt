package com.jb.rsm.settings

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.bind
import com.intellij.openapi.observable.util.whenItemSelected
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.BooleanTableCellEditor
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.TableUtil
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.MAX_LINE_LENGTH_WORD_WRAP
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import com.jb.rsm.util.Messages.message


private abstract class CodeTemplateColumnInfo<Aspect>(
    name: String,
    private val editable: Boolean,
    private val property: KMutableProperty1<CodeTemplate, Aspect>,
    private val columnClass: Class<Aspect>
) : ColumnInfo<CodeTemplate, Aspect>(name) {

    override fun valueOf(item: CodeTemplate): Aspect {
        return property.get(item)
    }

    override fun getColumnClass(): Class<*> {
        return columnClass
    }

    override fun isCellEditable(item: CodeTemplate?): Boolean {
        return editable
    }

    override fun setValue(item: CodeTemplate, value: Aspect?) {
        if (editable && value != null) {
            property.set(item, value)
        }
    }
}

fun <R> KMutableProperty0<R>.toObservableMutableProperty(): ObservableMutableProperty<R> {
    val property = this
    return object : ObservableMutableProperty<R> {
        override fun set(value: R) {
            property.set(value)
        }

        override fun get(): R {
            return property.get()
        }

        override fun afterChange(listener: (R) -> Unit) {
            // Ignore
        }

        override fun afterChange(listener: (R) -> Unit, parentDisposable: Disposable) {
            // Ignore
        }
    }
}

class CodeTemplateTableModel(val project: Project, items: List<CodeTemplate>) : ListTableModel<CodeTemplate>(arrayOf(
    object : CodeTemplateColumnInfo<Boolean>(" ", true, CodeTemplate::generate, Boolean::class.java) {
        val renderer = BooleanTableCellRenderer()

        override fun getEditor(item: CodeTemplate): TableCellEditor {
            return BooleanTableCellEditor()
        }

        override fun getRenderer(item: CodeTemplate): TableCellRenderer {
            return renderer
        }
    },
    object : CodeTemplateColumnInfo<String>(
        message("ui.setting.templates.column.templateName"),
        false,
        CodeTemplate::templateName,
        String::class.java
    ) {
    },
    object : CodeTemplateColumnInfo<OverwriteStrategy>(
        message("ui.setting.templates.column.overwriteStrategy"),
        true,
        CodeTemplate::overwriteStrategy,
        OverwriteStrategy::class.java
    ) {
        override fun getRenderer(item: CodeTemplate?): TableCellRenderer {
            val renderer = object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable?,
                    value: Any?,
                    isSelected: Boolean,
                    hasFocus: Boolean,
                    row: Int,
                    column: Int
                ): Component {
                    if (value is OverwriteStrategy) {
                        text = formatOverwriteStrategy(value)
                    }
                    return super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column)
                }
            }

            return renderer
        }

        override fun getEditor(item: CodeTemplate): TableCellEditor {
            val dropDown = object : ComboBox<OverwriteStrategy>(
                item.overwriteStrategies.toTypedArray()
            ) {
                init {
                    prototypeDisplayValue = OverwriteStrategy.OnlyIfGenerated

                    setRenderer(object : DefaultListCellRenderer() {
                        override fun getListCellRendererComponent(
                            list: JList<*>?,
                            value: Any?,
                            index: Int,
                            isSelected: Boolean,
                            cellHasFocus: Boolean
                        ): Component {
                            if (value is OverwriteStrategy) {
                                text = formatOverwriteStrategy(value)
                            }
                            super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus)

                            return this
                        }
                    })
                }
            }.bind(item::overwriteStrategy.toObservableMutableProperty())

            val dropDownEditor = object : AbstractTableCellEditor() {
                override fun getCellEditorValue(): Any {
                    return dropDown.selectedItem as OverwriteStrategy
                }

                override fun getTableCellEditorComponent(
                    table: JTable?,
                    value: Any?,
                    isSelected: Boolean,
                    row: Int,
                    column: Int
                ): Component {
                    return dropDown
                }
            }

            dropDown.whenItemSelected {
                dropDownEditor.stopCellEditing()
            }

            dropDown.registerTableCellEditor(dropDownEditor)

            return dropDownEditor
        }

        private fun formatOverwriteStrategy(overwriteStrategy: OverwriteStrategy): String {
            // Insert spaces before capital letters
            val name = overwriteStrategy.name.replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
            return """<html><body>${name}<br><em>${overwriteStrategy.description}</em></body></html>"""
        }
    }
), items)

class CodeTemplateTable(project: Project, items: List<CodeTemplate>) :
    TableView<CodeTemplate>(CodeTemplateTableModel(project, items)) {
    init {
        autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS

        TableUtil.setupCheckboxColumn(this, 0)
    }
}

class ResultSetMapperSettingsUIComponent(private val project: Project) {
    @JvmField
    val panel: DialogPanel

    init {
        panel = createPanel()
    }

    private fun createPanel(): DialogPanel {
        val settings = getResultMapperSettings(project).state

        return panel {
            row("${message("ui.setting.packageName")}:") {
                textField()
                    .comment(message("ui.setting.packageName.comment"),  MAX_LINE_LENGTH_WORD_WRAP)
                    .align(AlignX.FILL)
                    .columns(COLUMNS_LARGE)
                    .bindText(settings::packageName)
            }
            row("${message("ui.setting.fromRowClassFilter")}:") {
                textField()
                    .comment(message("ui.setting.fromRowClassFilter.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    .align(AlignX.FILL)
                    .columns(COLUMNS_LARGE)
                    .bindText(settings::fromRowClassFilter)
            }

            row("${message("ui.setting.templates")}:") {
                scrollCell(
                    CodeTemplateTable(project, settings.templates)
                ).align(AlignX.FILL).comment(
                    """
                    You can modify the ResultSetMapper templates in both the Code and Other tab in Settings | Editor | <a>File and Code Templates</a>.
                    """.trimIndent(),
                    MAX_LINE_LENGTH_WORD_WRAP
                ) {
                    DataManager.getInstance().dataContextFromFocusAsync.onSuccess { context ->
                        context?.let { dataContext ->
                            Settings.KEY.getData(dataContext)?.let { settings ->
                                settings.select(settings.find("fileTemplates"))
                            }
                        }
                    }
                }
            }
        }
    }
}
