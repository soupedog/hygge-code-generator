package hygge.plugin.generator.core.domain.bo

import hygge.commons.constant.ConstantParameters
import hygge.util.constant.ConstantClassInfoContainer.OFFSET_DATE_TIME
import hygge.util.definition.HyggeUtil
import hygge.util.generator.java.bo.ClassInfo
import hygge.util.generator.java.bo.JavaGeneratorConfiguration

class DatabaseConfiguration(
    var absolutePathOfProject: String,
    var hostPort: String,
    var userName: String,
    var password: String,
    var schema: String
) : JavaGeneratorConfiguration(), HyggeUtil {
    var enumElementInterval: Int = 1
    var enumNameSuffix: String = "Enum"
    var poPathSuffix: String = ".po"
    var basePoPathSuffix: String = ".base"
    var enumPathSuffix: String = ".enums"
    var defaultTimeType: ClassInfo = OFFSET_DATE_TIME
    var pathFromRepositoryRoot: String =
        ConstantParameters.FILE_SEPARATOR + "src" + ConstantParameters.FILE_SEPARATOR + "main" + ConstantParameters.FILE_SEPARATOR + "java"
    var packageInfo: String = "hygge.domain"
}