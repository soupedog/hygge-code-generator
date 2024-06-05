package hygge.plugin.generator.component.toolWindow.service

import hygge.plugin.generator.language.LanguageEnum
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import hygge.plugin.generator.language.EnBundle

@Service(Service.Level.PROJECT)
class MyToolWindowService(project: Project) {
    private var currentLanguage: LanguageEnum = LanguageEnum.English;

    init {
        thisLogger().info(EnBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `hygge.plugin.xml`.")
    }

    fun getRandomNumber() = (1..100).random()

    fun setCurrentLanguage(language: LanguageEnum? = null) {
        if (language == null) {
            if (LanguageEnum.English == currentLanguage) {
                currentLanguage = LanguageEnum.简体中文
            } else {
                currentLanguage = LanguageEnum.English
            }
        } else {
            currentLanguage = language
        }
    }

    fun getCurrentLanguage(): LanguageEnum {
        return currentLanguage
    }
}
