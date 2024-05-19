package com.jb.rsm

import com.intellij.openapi.application.ReadAction
import com.intellij.pom.java.LanguageLevel
import com.jb.rsm.framework.IntentionActionTest
import com.jb.rsm.util.Messages.message
import org.junit.jupiter.api.Test

class ResultSetMapperTest : IntentionActionTest(LanguageLevel.JDK_17_PREVIEW) {

    override fun getTestDataPath() = "src/test/testData/resultSetMapper"
    override fun getIntentionName() = message("intention.resultSetMapper.familyName")
    override fun getDependencies() = listOf("org.springframework:spring-jdbc:6.1.7")

    @Test
    fun testResultSetMapperOnQueryMethod() {
        doTest()

        val packageName = settings.packageName
        ReadAction.run<Exception> {
            for (className in listOf("ResultSetWrapper", "ResultSetMapper", "FromRowMapper")) {
                fixture.findClass("${packageName}.${className}")
            }
        }
    }
}
