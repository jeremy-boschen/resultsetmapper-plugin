@file:Suppress("MemberVisibilityCanBePrivate")

package com.jb.rsm.framework

import com.intellij.JavaTestUtil
import com.intellij.openapi.project.Project
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.testFramework.rules.TestNameExtension
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.ui.UIUtil
import com.jb.rsm.settings.ResultSetMapperSettings
import com.jb.rsm.settings.getResultMapperSettings
import org.jetbrains.annotations.NotNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.extension.RegisterExtension

abstract class IntentionActionTest(languageLevel: LanguageLevel = LanguageLevel.JDK_11) {
    protected abstract fun getIntentionName(): String
    protected abstract fun getTestDataPath(): String

    protected open fun getRelativePath(): String = JavaTestUtil.getRelativeJavaTestDataPath()
    protected open fun getDependencies(): List<String> = emptyList()

    @JvmField
    @RegisterExtension
    protected val testCase = object : JdkFixtureTestCase(languageLevel) {
        override fun getTestDataPath(): String = this@IntentionActionTest.getTestDataPath()
        override fun getBasePath(): String = this@IntentionActionTest.getRelativePath()
        override fun getDependencies(): List<String> = this@IntentionActionTest.getDependencies()
    }

    protected val fixture: JavaCodeInsightTestFixture get() = testCase.fixture
    protected val project: @NotNull Project get() = fixture.project
    protected val settings: ResultSetMapperSettings.State get() = getResultMapperSettings(project).state

    @JvmField
    @RegisterExtension
    protected val testNameRule = TestNameExtension()

    protected fun getTestName(): String {
        return PlatformTestUtil.getTestName(testNameRule.methodName, false)
    }

    protected fun doTest() {
        val dataFileGroup = getTestName() + ".java"

        fixture.configureByFile("/before_$dataFileGroup")
        performIntentionTest()
        fixture.checkResultByFile("/after_$dataFileGroup")
    }

    protected fun performIntentionTest() {
        val intention = fixture.findSingleIntention(getIntentionName())
        assertNotNull(intention, "Intention ${getIntentionName()} not found. Please check intention name and verify that the intention's isAvailable method returns true.")
        fixture.launchAction(intention)
        runInEdtAndWait { UIUtil.dispatchAllInvocationEvents() }
    }
}