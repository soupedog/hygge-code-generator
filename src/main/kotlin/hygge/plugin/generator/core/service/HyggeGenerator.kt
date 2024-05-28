package hygge.plugin.generator.core.service

import hygge.util.UtilCreator
import hygge.util.definition.CollectionHelper
import hygge.util.definition.ParameterHelper
import hygge.util.generator.java.bo.ClassInfo

interface HyggeGenerator {
    val parameterHelper: ParameterHelper
        get() = UtilCreator.INSTANCE.getDefaultInstance(ParameterHelper::class.java)
    val collectionHelper: CollectionHelper
        get() = UtilCreator.INSTANCE.getDefaultInstance(CollectionHelper::class.java)

    fun parseIntoClassName(underscoreToCamelCase: Boolean, tableOrColumnName: String): String {
        var result: String
        if (underscoreToCamelCase) {
            val resultTemp = tableOrColumnName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            result = ""
            for (item in resultTemp) {
                result = result + parameterHelper.upperCaseFirstLetter(item)
            }
        } else {
            result = parameterHelper.upperCaseFirstLetter(tableOrColumnName)
        }
        return result
    }

    fun getAttributeName(underscoreToCamelCase: Boolean, target: String): String {
        var result: String
        if (underscoreToCamelCase) {
            val resultTemp = target.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            result = ""
            for (i in resultTemp.indices) {
                val item = resultTemp[i]
                result = if (i == 0) {
                    result + item
                } else {
                    result + parameterHelper.upperCaseFirstLetter(item)
                }
            }
        } else {
            result = target
        }
        return result
    }

    fun isMatchAbstractClass(abstractClassInfo: ClassInfo, target: ClassInfo): Boolean {
        var matchPropertyCount = 0

        for (property in abstractClassInfo.properties) {
            val propertyName = property.name

            for (targetProperty in target.properties) {
                // 目标类信息中，包含基类中的一个属性，计数器 +1
                if (targetProperty.name == propertyName) {
                    matchPropertyCount += 1
                    break
                }
            }
        }

        // 目标类信息中，匹配成功的属性数目与基类属性完全一致则代表匹配
        return matchPropertyCount == abstractClassInfo.properties.size
    }
}