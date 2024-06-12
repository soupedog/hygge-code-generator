package hygge.plugin.generator.component.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import hygge.plugin.generator.language.LanguageEnum

@Service(Service.Level.PROJECT)
class HyggeGeneratorToolWindowService(project: Project) {
    private var currentLanguage: LanguageEnum = LanguageEnum.English;

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
