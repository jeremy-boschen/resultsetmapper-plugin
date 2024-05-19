package com.jb.rsm.intentions.codegen

import com.intellij.icons.AllIcons
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory


class ResultSetMapperTemplateGroup : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("ResultSetMapper", AllIcons.FileTypes.Java)
        with(group) {
            addTemplate(FROMROW_MAPPER_TEMPLATE)
            addTemplate(RESULTSET_MAPPER_TEMPLATE)
            addTemplate(RESULTSET_WRAPPER_TEMPLATE)
        }
        return group
    }

    companion object {
        // File templates
        const val FROMROW_MAPPER_TEMPLATE = "FromRowMapper.java"
        const val RESULTSET_MAPPER_TEMPLATE = "ResultSetMapper.java"
        const val RESULTSET_WRAPPER_TEMPLATE = "ResultSetWrapper.java"

        // Code templates
        const val RESULTSETMAPPER_FROMROW_METHOD = "ResultSetMapper fromRow Method.java"
        const val RESULTSETMAPPER_LAMBDA_ARGUMENT = "ResultSetMapper JDBC query Method Argument.java"
    }
}
