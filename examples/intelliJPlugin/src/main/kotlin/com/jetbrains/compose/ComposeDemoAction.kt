package com.jetbrains.compose

import androidx.compose.desktop.ComposePanel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import com.jetbrains.compose.widgets.Buttons
import com.jetbrains.compose.widgets.LazyScrollable
import com.jetbrains.compose.widgets.Loaders
import com.jetbrains.compose.widgets.TextInputs
import com.jetbrains.compose.widgets.Toggles
import java.awt.Dimension
import javax.swing.JComponent


/**
 * @author Konstantin Bulenkov
 */
class ComposeDemoAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        DemoDialog(e.project).show()
    }

    class DemoDialog(project: Project?) : DialogWrapper(project) {
        init {
            title = "Demo"
            init()
        }

        override fun createCenterPanel(): JComponent {
            val dialog = this
            var packed = false
            return ComposePanel().apply {
                setContent {
                    ComposeSizeAdjustmentWrapper(
                        window = dialog,
                        panel = this,
                        preferredSize = IntSize(800, 600)
                    ) {
                        Row {
                            Column(
                                modifier = Modifier.fillMaxHeight().weight(1f)
                            ) {
                                Buttons()
                                Loaders()
                                Toggles()
                            }
                            Box(
                                modifier = Modifier.fillMaxHeight().weight(1f)
                            ) {
                                LazyScrollable()
                            }
                        }
                    }
                }
            }
        }
    }
}
