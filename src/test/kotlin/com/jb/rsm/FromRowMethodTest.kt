package com.jb.rsm

import com.intellij.openapi.application.ReadAction
import com.jb.rsm.framework.IntentionActionTest
import com.jb.rsm.util.Messages.message
import org.junit.jupiter.api.Test

class FromRowMethodTest : IntentionActionTest() {

    override fun getTestDataPath() = "src/test/testData/fromRowMethod"
    override fun getIntentionName() = message("intention.fromRowMethod.familyName")

    @Test
    fun testFromRowMethodCreatedOnDTO() {
        doTest()

        val packageName = settings.packageName
        ReadAction.run<Exception> {
            for (className in listOf("ResultSetWrapper", "FromRowMapper")) {
                fixture.findClass("${packageName}.${className}")
            }
        }
    }
}
