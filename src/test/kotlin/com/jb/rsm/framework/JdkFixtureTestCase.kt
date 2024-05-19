package com.jb.rsm.framework

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.MavenDependencyUtil
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext

open class JdkFixtureTestCase(languageLevel: LanguageLevel = LanguageLevel.JDK_17_PREVIEW) :
    LightJavaCodeInsightFixtureTestCase(), BeforeEachCallback,
    AfterEachCallback, Extension {

    private val projectDescriptor = object: ProjectDescriptor(languageLevel) {
        override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
           super.configureModule(module, model, contentEntry)

            for (dependency in getDependencies()) {
                MavenDependencyUtil.addFromMaven(model, dependency, true)
            }
        }
    }

    open fun getDependencies(): List<String> = emptyList()

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return projectDescriptor
    }

    override fun beforeEach(context: ExtensionContext?) {
        setUp()
    }

    override fun afterEach(context: ExtensionContext?) {
        tearDown()
    }

    val fixture: JavaCodeInsightTestFixture get() = myFixture
}