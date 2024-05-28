package hygge.plugin.generator.component.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import hygge.plugin.generator.component.toolWindow.service.MyToolWindowService
import hygge.plugin.generator.core.domain.bo.DatabaseConfiguration
import hygge.plugin.generator.core.service.topo.PoGenerator
import hygge.plugin.generator.util.BundleUtil
import hygge.util.UtilCreator
import hygge.util.constant.ConstantClassInfoContainer
import hygge.util.definition.CollectionHelper
import hygge.util.generator.java.bo.ClassInfo
import hygge.util.generator.java.bo.Modifier
import hygge.util.generator.java.bo.Property
import javax.swing.JButton

/**
 * 新建一个工具栏(和 project 栏类似)，中间包含一个按钮，每次点击生成一个随机数并切换语言
 */
class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("MyToolWindowFactory start.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(project), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyToolWindowService>()

        fun getContent(project: Project) = JBPanel<JBPanel<*>>().apply {
            val labelInitText: String = BundleUtil.message(service.getCurrentLanguage(), "randomLabel", "?")
            val label = JBLabel(labelInitText)

            val button = createButton(project, label)

            add(label)
            add(button)
        }

        fun createButton(project: Project, label: JBLabel): JButton {
            val buttonText: String = BundleUtil.message(service.getCurrentLanguage(), "shuffle")
            val button = JButton(buttonText)

            // 点击事件监听
            button.addActionListener {
                // 切换语言
                service.setCurrentLanguage()

                val schema = "local_test"
                val databaseConfiguration = DatabaseConfiguration(project.basePath!!, "localhost:3306/", "root", "0000", schema)
                databaseConfiguration.enumElementInterval = 100
                databaseConfiguration.isLombokEnable = true

                var enumContainer: MutableMap<String, ClassInfo> = HashMap()

                var abstractClassInfoContainer: MutableMap<String, ClassInfo> = HashMap()

                val collectionHelper: CollectionHelper = UtilCreator.INSTANCE.getDefaultInstance(CollectionHelper::class.java)

                val basePo = ClassInfo.builder()
                    .name("BasePo")
                    .description("PO 对象基类")
                    .modifiers(collectionHelper.createCollection(Modifier.PUBLIC, Modifier.ABSTRACT))
                    .properties(
                        collectionHelper.createCollection(
                            Property.builder()
                                .name("createTs")
                                .classInfo(databaseConfiguration.defaultTimeType)
                                .modifiers(collectionHelper.createCollection(Modifier.PROTECTED))
                                .description("创建 UTC 毫秒级时间戳")
                                .build(),
                            Property.builder()
                                .name("lastUpdateTs")
                                .classInfo(databaseConfiguration.defaultTimeType)
                                .modifiers(collectionHelper.createCollection(Modifier.PROTECTED))
                                .description("最后修改 UTC 毫秒级时间戳")
                                .build()
                        )
                    )
                    .build()

                basePo.init(databaseConfiguration)
                basePo.annotations.remove(ConstantClassInfoContainer.BUILDER)
                basePo.annotations.remove(ConstantClassInfoContainer.ALL_ARGS_CONSTRUCTOR)

                abstractClassInfoContainer[basePo.name] = basePo

                PoGenerator(databaseConfiguration, enumContainer, abstractClassInfoContainer).generatePO()

                // 重新加载文本
                label.text = BundleUtil.message(service.getCurrentLanguage(), "randomLabel", service.getRandomNumber())
                button.text = BundleUtil.message(service.getCurrentLanguage(), "shuffle")
            }
            return button
        }
    }
}
