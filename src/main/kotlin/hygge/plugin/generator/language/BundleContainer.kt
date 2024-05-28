package hygge.plugin.generator.language

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val EN_BUNDLE = "messages.en_Bundle"

@NonNls
private const val ZH_BUNDLE = "messages.zh_Bundle"

/**
 * 看起来是个教你如何从配置文件中读取内容的示例(可以用来支持国际化，配置多种语言的 Bundle 配置，再通过当前语言标识位动态切换文本)
 */
object EnBundle : DynamicBundle(EN_BUNDLE) {

    // @JvmStatic 是为 kt → java 代码转换的指导性标记，告诉 java 代码如何转化，单纯在 kt 代码里，这些注解意义不大
    // (官方示例代码我原样抄过来所以没删除)
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = EN_BUNDLE) key: String, vararg params: Any) =
            getMessage(key, *params)

    @Suppress("unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = EN_BUNDLE) key: String, vararg params: Any) =
            getLazyMessage(key, *params)
}

object ZhBundle : DynamicBundle(ZH_BUNDLE) {

    // @JvmStatic 是为 kt → java 代码转换的指导性标记，告诉 java 代码如何转化，单纯在 kt 代码里，这些注解意义不大
    // (官方示例代码我原样抄过来所以没删除)
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = ZH_BUNDLE) key: String, vararg params: Any) =
            getMessage(key, *params)

    @Suppress("unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = ZH_BUNDLE) key: String, vararg params: Any) =
            getLazyMessage(key, *params)
}
