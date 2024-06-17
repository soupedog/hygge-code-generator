package hygge.plugin.generator.core.domain.bo

import hygge.commons.constant.ConstantParameters
import hygge.util.generator.java.bo.JavaGeneratorConfiguration

class DatabaseConfiguration(
    var absolutePathOfProject: String,
    var hostPort: String,
    var userName: String,
    var password: String,
    var schema: String
) : JavaGeneratorConfiguration() {
    var underscoreToCamelCaseEnable: Boolean = false
    var enumElementInterval: Int = 1
    var enumPathSuffix: String = ".enums"
    var enumNameSuffix: String = "Enum"
    var enumInfoJson: String = "[]"
    var poPathSuffix: String = ".po"
    var poBasePathSuffix: String = ".base"
    var baseInfoJson: String = "[]"
    var defaultTimeType: TimeClassInfoEnum = TimeClassInfoEnum.ZONED_DATE_TIME
    var pathFromRepositoryRoot: String = ConstantParameters.FILE_SEPARATOR + "src" + ConstantParameters.FILE_SEPARATOR + "main" + ConstantParameters.FILE_SEPARATOR + "java"
    var packageInfo: String = "hygge.domain"
}