package com.jb.rsm.intentions.codegen

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSETMAPPER_FROMROW_METHOD
import com.jb.rsm.settings.OverwriteStrategy
import com.jb.rsm.util.Messages.message
import com.jb.rsm.util.isTypeOf

class GenerateFromRowMethod(
    project: Project,
    element: PsiElement,
    private val recordType: PsiClassType
) : CodeGenTask(project, element) {

    override val name: String
        get() {
            return message("codegen.fromRowMethod.title", "${recordType.presentableText}::fromRow")
        }

    private val template = RESULTSETMAPPER_FROMROW_METHOD

    private fun getFieldProperties(field: PsiField): Map<String, Any?> {
        val sqlType = sqlTypeOf(field.type)

        return mapOf(
            "isValid" to (sqlType.first != null),
            "isArray" to (sqlType.first == "Array"),
            "isList" to (sqlType.first == "List"),
            "isSet" to (sqlType.first == "Set"),
            "isCollection" to (sqlType.first == "Collection"),
            "isInitialized" to field.hasInitializer(),
            "isPrimitive" to (field.type is PsiPrimitiveType),
            "field" to field,
            "type" to field.type,
            "name" to field.name,
            "componentType" to sqlType.second,
            "collectionType" to sqlType.second,
            "columnType" to (sqlType.first ?: ""),
            "columnName" to field.name.lowercase().replace("_", "")
        )
    }

    override fun run() {
        val templateSettings = settings.getTemplateNamed(template)
        if (templateSettings?.generate == false) {
            log.info("Skipping generate task due to $template settings: generate=false")

            return
        }

        val classType = recordType.resolve() ?: return

        val columnFields = classType.allFields.filter {
            !it.hasModifierProperty(PsiModifier.STATIC) && !it.hasModifierProperty(PsiModifier.FINAL)
        }.map { field ->
            getFieldProperties(field)
        }

        // Prepare properties for the ResultSetMapper fromRow Method.java.ft template
        val properties = mapOf(
            "class" to classType,
            "className" to classType.name,
            "fields" to columnFields,
        )

        val methodText = generateCodeTemplate(template, properties)
        val method = elementFactory.createMethodFromText(methodText, classType)
        addOrReplaceMethod(
            classType,
            method,
            OverwriteStrategySetting(template, templateSettings?.overwriteStrategy ?: OverwriteStrategy.Always)
        )
    }

    private fun sqlTypeOf(type: PsiType): Pair<String?, PsiType?> {
        log.info("Determining SQL type for ${type.presentableText}")

        val mappedType = TypeMap[type]
        if (mappedType != null) {
            return Pair(mappedType, type)
        }

        if (type is PsiArrayType) {
            if (type.componentType in TypeMap) {
                return Pair("Array", type.componentType)
            }

            return Pair(null, null)
        }

        if (type is PsiClassType) {
            val resolved = type.resolve()
            if (resolved != null) {
                val elementType = type.parameters.firstOrNull()
                    ?: elementFactory.createTypeFromText("java.lang.Object", null)

                if (resolved.isTypeOf("java.util.List")) {
                    if (TypeMap.contains(elementType)) {
                        return Pair("List", elementType)
                    }
                }
                else if (resolved.isTypeOf("java.util.Set")) {
                    if (TypeMap.contains(elementType)) {
                        return Pair("Set", elementType)
                    }
                }
                else if (resolved.isTypeOf("java.util.Collection")) {
                    if (TypeMap.contains(elementType)) {
                        return Pair("Collection", elementType)
                    }
                }
            }
        }

        return Pair(null, null)
    }

    private object TypeMap {
        operator fun get(type: PsiType): String? {
            return lookup(type.canonicalText)
        }

        operator fun contains(type: PsiType): Boolean {
            return lookup(type.canonicalText) != null
        }

        operator fun get(type: String): String? {
            return lookup(type)
        }

        operator fun contains(type: String): Boolean {
            return lookup(type) != null
        }

        private fun lookup(type: String): String? {
            val shortName = type.substringAfterLast('.').lowercase()
            return psiTypeToSqlColumnType[shortName]
        }

        private val psiTypeToSqlColumnType = mapOf(
            "object" to "Object",
            "boolean" to "Boolean",
            "char" to "Char",
            "character" to "Char",
            "byte[]" to "Binary",
            "byte" to "Byte",
            "short" to "Short",
            "float" to "Float",
            "double" to "Double",
            "int" to "Int",
            "integer" to "Int",
            "long" to "Long",
            "bigdecimal" to "BigDecimal",
            "string" to "String",
            "date" to "Date",
            "time" to "Time",
            "timestamp" to "Timestamp",
            "array" to "Array",
            "uuid" to "UUID",
            "localdate" to "LocalDate",
            "localdatetime" to "LocalDateTime",
            "offsetdatetime" to "OffsetDateTime",
        )
    }
}