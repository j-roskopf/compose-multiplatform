/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.r4a.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.quickfix.KotlinQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.r4a.R4aFqNames
import org.jetbrains.kotlin.r4a.analysis.R4AErrors

// TODO(lmr): we should do the same with @Children
class AnnotateTargetWithComposableQuickFix(private val expression: KtElement) : KotlinQuickFixAction<KtElement>(expression) {
    companion object MyFactory : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val target = R4AErrors.NON_COMPOSABLE_INVOCATION.cast(Errors.PLUGIN_WARNING.cast(diagnostic).a.diagnostic).b
            val foundPsiElement = target.findPsi() as? KtElement ?: return null

            return AnnotateTargetWithComposableQuickFix(foundPsiElement)
        }
    }

    override fun getFamilyName() = "KTX"

    // TODO(lmr): we should pass in the name and use it here
    override fun getText() = "Annotate with ''@Composable''"

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
//        val ktPsiFactory = KtPsiFactory(project, markGenerated = true)

        when (expression) {
            is KtProperty -> {
                val typeRef = expression.typeReference
                val defaultValue = expression.initializer
                if (typeRef != null) {
                    typeRef.addAnnotation(R4aFqNames.Composable, "")
                } else if (defaultValue != null && defaultValue is KtLambdaExpression) {
                    // do it to default value...
                    defaultValue.addAnnotation(R4aFqNames.Composable)
                } else {
                    expression.addAnnotation(R4aFqNames.Composable)
                }
            }
            is KtParameter -> {
                // if there's a type reference, add it to that
                // if not, try adding it to the expression...?
                val typeRef = expression.typeReference
                val defaultValue = expression.defaultValue
                if (typeRef != null) {
                    typeRef.addAnnotation(R4aFqNames.Composable, "")
                } else if (defaultValue != null && defaultValue is KtLambdaExpression) {
                    // do it to default value...
                    defaultValue.addAnnotation(R4aFqNames.Composable)
                } else {
                    expression.addAnnotation(R4aFqNames.Composable)
                }
            }
            is KtNamedFunction -> expression.addAnnotation(R4aFqNames.Composable)
            else -> error("Unknown element type: ${expression.node.elementType}")
        }
    }
}

private fun KtLambdaExpression.addAnnotation(
    annotationFqName: FqName,
    annotationInnerText: String? = null
): Boolean {
    val annotationText = when (annotationInnerText) {
        null -> "@${annotationFqName.asString()}"
        else -> "@${annotationFqName.asString()}($annotationInnerText)"
    }

    val psiFactory = KtPsiFactory(this)
    val annotatedExpression = psiFactory.createExpression("$annotationText $text")
    val parent = parent as? KtElement
    replace(annotatedExpression)
    parent?.let { ShortenReferences.DEFAULT.process(it) }

    return true
}