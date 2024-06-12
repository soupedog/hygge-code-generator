package hygge.plugin.generator.component.toolWindow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.jetbrains.rd.swing.selectedItemProperty
import hygge.plugin.generator.component.toolWindow.service.MyToolWindowService
import hygge.plugin.generator.core.domain.bo.DatabaseConfiguration
import hygge.plugin.generator.core.domain.bo.TimeClassInfoEnum
import hygge.plugin.generator.core.service.topo.MysqlDatabaseInfoScanner
import hygge.plugin.generator.core.service.topo.PoGenerator
import hygge.plugin.generator.language.LanguageEnum
import hygge.plugin.generator.util.BundleUtil
import hygge.plugin.generator.util.NotificationsUtil
import hygge.util.UtilCreator
import hygge.util.constant.ConstantClassInfoContainer
import hygge.util.definition.CollectionHelper
import hygge.util.definition.ParameterHelper
import hygge.util.generator.java.bo.ClassInfo
import hygge.util.generator.java.bo.Modifier
import hygge.util.generator.java.bo.Property
import hygge.util.json.jackson.impl.DefaultJsonHelper
import java.util.*
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
        // 默认收起工具栏
        toolWindow.hide()
        myToolWindow.initComponent(project)

        val content = ContentFactory.getInstance().createContent(myToolWindow.mainContent, null, false)

        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private var parentDisposable = toolWindow.disposable
        private val service = toolWindow.project.service<MyToolWindowService>()
        private val parameterHelper = UtilCreator.INSTANCE.getDefaultInstance(ParameterHelper::class.java)
        private val collectionHelper: CollectionHelper = UtilCreator.INSTANCE.getDefaultInstance(CollectionHelper::class.java)
        var configuration = DatabaseConfiguration("", "localhost:3306/", "", "", "")
        val mainContent: JBPanel<JBPanel<*>> = JBPanel<JBPanel<*>>()
        lateinit var panel: DialogPanel
        val hostPortJBTextField: JBTextField = JBTextField()
        val userNameJBTextField: JBTextField = JBTextField()
        val passwordJBPasswordField: JBPasswordField = JBPasswordField()
        val languageComboBox = ComboBox(EnumComboBoxModel(LanguageEnum::class.java))
        val timeClassInfoEnumComboBox = ComboBox(EnumComboBoxModel(TimeClassInfoEnum::class.java))
        val generateButton = JButton()
        var schemaCell: Cell<ComboBox<String>>? = null

        fun initComponent(project: Project) {
            // 初始化项目根目录
            configuration.absolutePathOfProject = Optional.ofNullable(project.basePath).orElse("")

            // 检测到语言切换时刷新组件文本
            languageComboBox.addActionListener {
                val comboBox: ComboBox<LanguageEnum> = it.source as ComboBox<LanguageEnum>
                val currentType = comboBox.selectedItemProperty()

                if (service.getCurrentLanguage() != currentType.value) {
                    // 与旧类型不同才需要刷新(数据没变的情景，重绘 UI 异常缓慢)
                    service.setCurrentLanguage(currentType.value!!)
                    freshConfigurationPanel(currentType.value!!)
                }
            }

            // 点击生成按钮时，进行代码自动生成
            generateButton.addActionListener {
                val jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance<ObjectMapper>(true)
                println(jsonHelper.formatAsString(configuration))
                // 生成代码
            }

            freshConfigurationPanel(service.getCurrentLanguage())
        }

        fun freshConfigurationPanel(languageType: LanguageEnum): DialogPanel {
            var needRemove = false

            if (this::panel.isInitialized) {
                needRemove = true
            }

            panel = panel {
                group(BundleUtil.message(languageType, "databaseConfigurationTitle")) {
                    row {
                        comboBox(listOf("MySQL"))
                            .label(BundleUtil.message(languageType, "databaseTypeJBLabel"))

                        cell(hostPortJBTextField).horizontalAlign(HorizontalAlign.FILL)
                            .bindText(configuration::hostPort)
                            .label(BundleUtil.message(languageType, "hostPortJBLabel"))
                    }
                    row(BundleUtil.message(languageType, "userNameJBLabel")) {
                        cell(userNameJBTextField).horizontalAlign(HorizontalAlign.FILL)
                            .bindText(configuration::userName)
                    }
                    row(BundleUtil.message(languageType, "passwordJBLabel")) {
                        cell(passwordJBPasswordField).horizontalAlign(HorizontalAlign.FILL)
                            .bindText(configuration::password)
                    }
                    row {
                        schemaCell = comboBox(ArrayList<String>())
                            .bindItem(configuration::schema.toNullableProperty())
                            .label(BundleUtil.message(languageType, "schemaJBLabel"))
                        button(BundleUtil.message(languageType, "fetchSchemeButton")) {
                            // 参数校验
                            if (parameterHelper.isEmpty(hostPortJBTextField.text)) {
                                NotificationsUtil.warn(BundleUtil.message(languageType, "warningTextEmpty", BundleUtil.message(languageType, "hostPortJBLabel")))
                                return@button
                            }
                            if (parameterHelper.isEmpty(userNameJBTextField.text)) {
                                NotificationsUtil.warn(BundleUtil.message(languageType, "warningTextEmpty", BundleUtil.message(languageType, "userNameJBLabel")))
                                return@button
                            }
                            if (parameterHelper.isEmpty(String(passwordJBPasswordField.password))) {
                                NotificationsUtil.warn(BundleUtil.message(languageType, "warningTextEmpty", BundleUtil.message(languageType, "passwordJBLabel")))
                                return@button
                            }

                            try {// 链接数据库拉取 Schema
                                val mysqlDatabaseInfoScanner = MysqlDatabaseInfoScanner(
                                    hostPortJBTextField.text,
                                    userNameJBTextField.text,
                                    String(passwordJBPasswordField.password)
                                )

                                val schemaNameList = mysqlDatabaseInfoScanner.getSchemaNameList()
                                // 清空选项
                                schemaCell!!.component.removeAllItems()
                                schemaNameList.forEach {
                                    schemaCell!!.component.addItem(it)
                                }
                                NotificationsUtil.info(BundleUtil.message(languageType, "infoTextSchemaFetchComplete"))
                            } catch (e: Exception) {
                                NotificationsUtil.error(
                                    BundleUtil.message(
                                        languageType, "errorTextDatabaseConnectionError",
                                        userNameJBTextField.text,
                                        hostPortJBTextField.text
                                    )
                                )
                            }

                        }.horizontalAlign(HorizontalAlign.RIGHT)
                    }
                }

                group(BundleUtil.message(languageType, "generatorConfigurationTitle")) {
                    row {
                        textField().horizontalAlign(HorizontalAlign.FILL)
                            .bindText(configuration::packageInfo)
                            .label(BundleUtil.message(languageType, "outputPackageInfoJBLabel"))
                    }

                    row {
                        textField()
                            .bindText(configuration::getAuthor, configuration::setAuthor)
                            .label(BundleUtil.message(languageType, "authorJBLabel"))

                        textField().horizontalAlign(HorizontalAlign.FILL)
                            .bindText(configuration::getDate, configuration::setDate)
                            .label(BundleUtil.message(languageType, "dateJBLabel"))
                    }

                    row {
                        checkBox(BundleUtil.message(languageType, "lombokEnableJBLabel"))
                            .bindSelected(configuration::isLombokEnable, configuration::setLombokEnable)

                        checkBox(BundleUtil.message(languageType, "underscoreToCamelCaseEnableJBLabel"))
                            .bindSelected(configuration::underscoreToCamelCaseEnable)

                    }

                    group(BundleUtil.message(languageType, "poConfigurationTitle")) {
                        row {
                            cell(timeClassInfoEnumComboBox)
                                .bindItem(configuration::defaultTimeType.toNullableProperty())
                                .label(BundleUtil.message(languageType, "poDefaultTimeTypeJBLabel"))
                        }
                        row {
                            textField()
                                .bindText(configuration::poPathSuffix)
                                .label(BundleUtil.message(languageType, "poPathSuffixJBLabel"))

                            textField().horizontalAlign(HorizontalAlign.FILL)
                                .bindText(configuration::poBasePathSuffix)
                                .label(BundleUtil.message(languageType, "poBaseClassPathSuffixJBLabel"))
                        }
                        row {
                            expandableTextField().horizontalAlign(HorizontalAlign.FILL)
                                .label(BundleUtil.message(languageType, "poBaseClassInfoJBLabel"))
                        }
                    }

                    group(BundleUtil.message(languageType, "enumConfigurationTitle")) {
                        row {
                            spinner(0..100000)
                                .bindIntValue(configuration::enumElementInterval)
                                .label(BundleUtil.message(languageType, "enumElementIndexIntervalJBLabel"))

                            checkBox(BundleUtil.message(languageType, "enumPropertyModifiableJBLabel"))
                                .bindSelected(configuration::isEnumPropertyModifiable, configuration::setEnumPropertyModifiable)
                        }
                        row {
                            textField()
                                .bindText(configuration::enumPathSuffix)
                                .label(BundleUtil.message(languageType, "enumPathSuffixJBLabel"))
                            textField().horizontalAlign(HorizontalAlign.FILL)
                                .bindText(configuration::enumNameSuffix)
                                .label(BundleUtil.message(languageType, "enumNameSuffixJBLabel"))
                        }
                        row {
                            expandableTextField().horizontalAlign(HorizontalAlign.FILL)
                                .label(BundleUtil.message(languageType, "enumInfoJBLabel"))
                        }
                    }
                }

                group(BundleUtil.message(languageType, "generatorCommandTitle")) {
                    row {
                        cell(languageComboBox)
                            .label(BundleUtil.message(languageType, "languageJBLabel"))
                        button(BundleUtil.message(languageType, "generateButton")) {
                            // 如果未选择 schema
                            if (schemaCell!!.component.selectedItem == null) {
                                NotificationsUtil.warn(BundleUtil.message(languageType, "warningTextNoSchema"))
                            } else {
                                // 面板 bind 数据先进行保存同步
                                panel.apply()

                                var enumContainer: MutableMap<String, ClassInfo> = HashMap()
                                var abstractClassInfoContainer: MutableMap<String, ClassInfo> = HashMap()

                                // TODO 参数校验
                                val basePo = ClassInfo.builder()
                                    .name("BasePo")
                                    .description("PO 对象基类")
                                    .modifiers(collectionHelper.createCollection(Modifier.PUBLIC, Modifier.ABSTRACT))
                                    .properties(
                                        collectionHelper.createCollection(
                                            Property.builder()
                                                .name("createTs")
                                                .classInfo(configuration.defaultTimeType.classInfo)
                                                .modifiers(collectionHelper.createCollection(Modifier.PROTECTED))
                                                .description("创建 UTC 毫秒级时间戳")
                                                .build(),
                                            Property.builder()
                                                .name("lastUpdateTs")
                                                .classInfo(configuration.defaultTimeType.classInfo)
                                                .modifiers(collectionHelper.createCollection(Modifier.PROTECTED))
                                                .description("最后修改 UTC 毫秒级时间戳")
                                                .build()
                                        )
                                    )
                                    .build()

                                basePo.init(configuration)
                                basePo.annotations.remove(ConstantClassInfoContainer.BUILDER)
                                basePo.annotations.remove(ConstantClassInfoContainer.ALL_ARGS_CONSTRUCTOR)

                                abstractClassInfoContainer[basePo.name] = basePo


                                val jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance<ObjectMapper>(true)
                                println(jsonHelper.formatAsString(basePo))

                                PoGenerator(configuration, enumContainer, abstractClassInfoContainer).generatePO()

                            }
                        }.horizontalAlign(HorizontalAlign.RIGHT)
                    }
                }
            }

            if (needRemove) {
                mainContent.removeAll()
            }

            mainContent.add(panel)

            // 注册校验器
            val disposable = Disposer.newDisposable()
            panel.registerValidators(disposable)
            Disposer.register(parentDisposable, disposable)

            return panel
        }
    }
}