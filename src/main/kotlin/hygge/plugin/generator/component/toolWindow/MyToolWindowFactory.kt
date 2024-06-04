package hygge.plugin.generator.component.toolWindow

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.jetbrains.rd.swing.selectedItemProperty
import hygge.plugin.generator.component.toolWindow.service.MyToolWindowService
import hygge.plugin.generator.core.domain.bo.DatabaseConfiguration
import hygge.plugin.generator.core.service.topo.PoGenerator
import hygge.plugin.generator.language.LanguageEnum
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

        myToolWindow.bindComponent(project)

        val content = ContentFactory.getInstance().createContent(myToolWindow.mainContent, null, false)

        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val service = toolWindow.project.service<MyToolWindowService>()
        val mainContent: JBPanel<JBPanel<*>> = JBPanel<JBPanel<*>>()
        val languageComboBox = ComboBox(EnumComboBoxModel(LanguageEnum::class.java))
        val databaseConfigurationTitle: TitledSeparator = TitledSeparator()
        val userNameJBLabel: JBLabel = JBLabel()
        val userNameJBTextField: JBTextField = JBTextField()
        val passwordJBLabel: JBLabel = JBLabel()
        val passwordJBPasswordField: JBPasswordField = JBPasswordField()
        val generateButton = JButton()

        fun bindComponent(project: Project) {
            refreshText(service.getCurrentLanguage())

            mainContent.add(databaseConfigurationTitle)

            mainContent.add(userNameJBLabel)
            mainContent.add(userNameJBTextField)

            mainContent.add(passwordJBLabel)
            mainContent.add(passwordJBPasswordField)

            // 点击生成按钮时，进行代码自动生成
            generateButton.addActionListener {
                val jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance<ObjectMapper>(true)
                val databaseConfiguration = DatabaseConfiguration(
                    project.basePath!!,
                    "localhost",
                    userNameJBTextField.text,
                    String(passwordJBPasswordField.password),
                    "/local_test"
                )
                println(jsonHelper.formatAsString(databaseConfiguration))
                // 生成代码
            }
            mainContent.add(generateButton)

            // 检测到语言切换时刷新组件文本
            languageComboBox.addActionListener {
                val comboBox: ComboBox<LanguageEnum> = it.source as ComboBox<LanguageEnum>
                val currentType = comboBox.selectedItemProperty()
                refreshText(currentType.value!!)
            }

            mainContent.add(languageComboBox)
        }

        fun refreshText(languageType: LanguageEnum) {
            databaseConfigurationTitle.text = BundleUtil.message(languageType, "databaseConfigurationTitle")
            userNameJBLabel.text = BundleUtil.message(languageType, "userNameJBLabel")
            passwordJBLabel.text = BundleUtil.message(languageType, "passwordJBLabel")

            generateButton.text = BundleUtil.message(languageType, "generateButton")
        }

        fun createButton(project: Project, label: JBLabel): JButton {
            val buttonText: String = BundleUtil.message(service.getCurrentLanguage(), "shuffle")
            val button = JButton(buttonText)

            // 点击事件监听
            button.addActionListener {
                // 切换语言
                service.setCurrentLanguage()

                val schema = "local_test"
                val databaseConfiguration =
                    DatabaseConfiguration(project.basePath!!, "localhost:3306/", "root", "0000", schema)
                databaseConfiguration.enumElementInterval = 100
                databaseConfiguration.isLombokEnable = true

                var enumContainer: MutableMap<String, ClassInfo> = HashMap()

                var abstractClassInfoContainer: MutableMap<String, ClassInfo> = HashMap()

                val collectionHelper: CollectionHelper =
                    UtilCreator.INSTANCE.getDefaultInstance(CollectionHelper::class.java)

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