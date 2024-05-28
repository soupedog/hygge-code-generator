package hygge.plugin.generator.util

import hygge.plugin.generator.language.LanguageEnum
import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import hygge.plugin.generator.language.EnBundle
import hygge.plugin.generator.language.ZhBundle

object BundleUtil {

    fun message(language: LanguageEnum, key: String, vararg params: Any): @Nls String {
        val bundle = getBundle(language)
        return bundle.getMessage(key, *params)
    }

    private fun getBundle(language: LanguageEnum): DynamicBundle {
        return when (language) {
            LanguageEnum.EN -> EnBundle
            LanguageEnum.ZH -> ZhBundle
        }
    }
}