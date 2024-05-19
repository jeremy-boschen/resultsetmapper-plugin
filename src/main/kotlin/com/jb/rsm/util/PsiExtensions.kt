package com.jb.rsm.util

import com.intellij.psi.JavaResolveResult
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiType
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil

// PsiClass extensions

fun PsiClass.isTypeOf(vararg qualifiedNames: String): Boolean {
    return this.qualifiedName in qualifiedNames ||  this.supers.any { it.qualifiedName in qualifiedNames }
}


// PsiMethod extensions

/**
 * Compares whether this method has the same lines of code as the other method, excluding blank lines.
 */
fun PsiMethod.matches(other: PsiMethod): Boolean {
    val codeStyleManager = CodeStyleManager.getInstance(project)

    fun formatLines(element: PsiElement?): String {
        if (element == null) {
            return ""
        }

        return codeStyleManager.reformat(element).text.lines().filter { it.isNotBlank() }.joinToString("\n") { it.trim() }
    }

    return formatLines(this) == formatLines(other)
}

fun PsiMethod.parameterIndexOf(qualifiedNames: Set<String>):Int {
    return this.parameterList.parameters.indexOfFirst {
        it.type.canonicalText.substringBefore("<") in qualifiedNames
    }
}

// PsiMethodCallExpression extensions

/**
 * Gets the name of the method called by this expression.
 */
val PsiMethodCallExpression.methodName: String?
    get() {
        return this.methodExpression.referenceName
    }


/**
 * Resolves the method called by this expression.
 *
 * @param condition A function that takes a [PsiMethod] and a [JavaResolveResult] and returns a boolean value.
 * @return The first resolved method that satisfies the condition, or [JavaResolveResult.EMPTY] if no such method is found.
 */
fun PsiMethodCallExpression.resolveMethod(condition: (PsiMethod, JavaResolveResult) -> Boolean): JavaResolveResult {
    for (resolved in this.methodExpression.multiResolve(true)) {
        if (condition(resolved.element as PsiMethod, resolved)) {
            return resolved
        }
    }

    return JavaResolveResult.EMPTY
}

/**
 * Maps the result of resolving the method called by this expression.
 *
 * @param mapper A function that takes a [PsiMethod] and a [JavaResolveResult] and returns a result of type [R] or null.
 * @return The result of the [mapper] function, if it returns a non-null value. Otherwise, null.
 */
fun <R : Any> PsiMethodCallExpression.mapResolveMethod(mapper: (PsiMethod, JavaResolveResult) -> R?): R? {
    for (resolved in this.methodExpression.multiResolve(true)) {
        val result = mapper(resolved.element as PsiMethod, resolved)
        if (result != null) {
            return result
        }
    }

    return null
}


/**
 * Resolves the substituted method return type of this expression. For example, if this expression resolves to a method
 * that returns a generic type, this property will return the substituted type. e.g. List<T> -> List<String>
 */
val PsiMethodCallExpression.returnType: PsiType?
    get() {
        return this.mapResolveMethod { method, resolved ->
            resolved.substitutor.substitute(method.returnType)
        }
    }

// PsiElement extensions

/**
 * Applies the [mapper] function to the parent element of the specified type, if found.
 *
 * @param type The type of the parent element to find.
 * @param mapper A function that takes a [PsiElement] of the specified type and returns a result of type [R] or null.
 * @return The result of the [mapper] function, if it returns a non-null value. Otherwise, null.
 */
fun <T : PsiElement, R : Any?> PsiElement.mapParentOfType(type: Class<out T>, mapper: (T) -> R?): R? {
    var element: PsiElement? = this

    while (element != null) {
        if (type.isInstance(element)) {
            val castElement = type.cast(element)

            val result = mapper(castElement)
            if (result != null) {
                return result
            }
        }
        val previous = PsiTreeUtil.getParentOfType(element, type)
        if (previous == element) {
            break
        }
        element = previous
    }

    return null
}

/**
 * Finds the parent element of the specified type, if found, and applies the [condition] function to it.
 *
 * @param type The type of the parent element to find.
 * @param condition A function that takes a [PsiElement] of the specified type and returns a boolean value.
 * @return The first parent element that satisfies the [condition], or null if no such element is found.
 */
fun <T : PsiElement> PsiElement.parentOfType(type: Class<out T>, condition: (T) -> Boolean): T? {
    return this.mapParentOfType(type) {
        if (condition(it)) {
            it
        } else {
            null
        }
    }
}
