package hygge.plugin.generator.action

import hygge.plugin.generator.util.NotificationsUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile

/**
 * 插件功能主体
 */
class DemoAction : AnAction() {
    private val lineSeparator: String = System.lineSeparator()

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(LangDataKeys.EDITOR)

        if (editor != null) {
            // 用户选中的文本内容
            val selectedText = editor.caretModel.currentCaret.selectedText

            val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)
            // 用户选中文本所属文件的路径
            val path = psiFile?.virtualFile?.path

            Messages.showInfoMessage("当前文件是：$lineSeparator $path $lineSeparator 选择的内容是：$lineSeparator $selectedText", "提示信息")

            // 当前文件的完整文本
            val fileText: String? = e.getData(PlatformCoreDataKeys.FILE_TEXT)
            Messages.showInfoMessage("$fileText", "${psiFile?.name} 文件完整文本")
        } else {
            NotificationsUtil.warn("请先选择一个文件后再进行操作")
        }
    }
}
