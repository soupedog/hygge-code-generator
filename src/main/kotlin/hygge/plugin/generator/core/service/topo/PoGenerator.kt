package hygge.plugin.generator.core.service.topo

import hygge.commons.constant.ConstantParameters
import hygge.commons.exception.UtilRuntimeException
import hygge.plugin.generator.core.domain.bo.DatabaseConfiguration
import hygge.plugin.generator.core.domain.po.MysqlColumnInfo
import hygge.plugin.generator.core.domain.po.MysqlTablesInfo
import hygge.plugin.generator.core.service.HyggeGenerator
import hygge.util.UtilCreator
import hygge.util.constant.ConstantClassInfoContainer
import hygge.util.definition.FileHelper
import hygge.util.generator.java.JavaGenerator
import hygge.util.generator.java.bo.ClassInfo
import hygge.util.generator.java.bo.ClassType
import hygge.util.generator.java.bo.EnumElement
import hygge.util.generator.java.bo.Property

class PoGenerator(private var configuration: DatabaseConfiguration, private var enumContainer: MutableMap<String, ClassInfo>, private var abstractClassInfoContainer: MutableMap<String, ClassInfo>) : HyggeGenerator {

    fun generatePO() {
        val mysqlDatabaseInfoScanner = MysqlDatabaseInfoScanner(
            configuration.hostPort,
            configuration.userName,
            configuration.password
        )

        val tablesInfo: ArrayList<MysqlTablesInfo> = mysqlDatabaseInfoScanner.getTableInfoBySchemaName(configuration.schema)

        val classInfoReadFromDatabase: MutableList<ClassInfo> = ArrayList()

        // 初始化扩展类
        abstractClassInfoContainer.values.forEach { abstractClassInfo ->
            if (abstractClassInfo.packageInfo == null) {
                abstractClassInfo.packageInfo = configuration.packageInfo + configuration.poPathSuffix + configuration.poBasePathSuffix
            }
        }

        // 基类信息根据参数数量由大到小排序，因为继承有排他性，选拟合度最高的继承
        val baseClassInfoList = abstractClassInfoContainer.values.sortedWith(compareBy { it.properties.size }).reversed()

        // 初始化扩展类
        enumContainer.values.forEach { enumClassInfo ->
            if (enumClassInfo.packageInfo == null) {
                enumClassInfo.packageInfo = configuration.packageInfo + configuration.enumPathSuffix
            }
        }

        // 将数据库表解析成类信息
        tablesInfo.forEach { tableInfo ->
            val classInfo = ClassInfo()
            classInfo.init(configuration)
            classInfo.packageInfo = configuration.packageInfo + configuration.poPathSuffix
            classInfo.name = parseIntoClassName(configuration.underscoreToCamelCaseEnable, tableInfo.tableName!!)
            classInfo.description = tableInfo.tableComment

            val columns: ArrayList<MysqlColumnInfo> = mysqlDatabaseInfoScanner.getColumnInfoBySchemaTableName(configuration.schema, tableInfo.tableName!!)

            columns.forEach { column ->
                val propertyList: MutableList<Property> = classInfo.properties
                val property = Property.builder()
                    .name(getAttributeName(configuration.underscoreToCamelCaseEnable, column.columnName!!))
                    .classInfo(parseIntoClassInfo(column))
                    .description(column.columnComment)
                    .build()

                propertyList.add(property)
            }

            // 检测是否需要继承基类
            for (abstractClassInfo in baseClassInfoList) {
                // 继承有排他性，只继承第一个符合条件的类
                if (isMatchAbstractClass(abstractClassInfo, classInfo)) {
                    classInfo.parent = abstractClassInfo
                    break
                }
            }

            if (classInfo.parent != null) {
                // 为当前类清除已包含在基类中的属性
                classInfo.parent.properties.forEach { parentProperty ->
                    classInfo.properties.removeIf { it.name == parentProperty.name }
                }
            }

            classInfoReadFromDatabase.add(classInfo)
        }

        var absolutePathPrefixForDefault: String = configuration.absolutePathOfProject + ConstantParameters.FILE_SEPARATOR + configuration.pathFromRepositoryRoot + ConstantParameters.FILE_SEPARATOR
        absolutePathPrefixForDefault = absolutePathPrefixForDefault.replace(".", ConstantParameters.FILE_SEPARATOR)

        // 生成初始枚举类
        saveAsJavaFile(absolutePathPrefixForDefault, enumContainer.values)

        // 生成初始基类
        saveAsJavaFile(absolutePathPrefixForDefault, abstractClassInfoContainer.values)

        // 生成从数据库解析而来的类/枚举
        saveAsJavaFile(absolutePathPrefixForDefault, classInfoReadFromDatabase)
    }

    /**
     * 根据类信息集合，在指定的磁盘目录中生成类文件
     *
     */
    fun saveAsJavaFile(absolutePathPrefixForDefault: String, classInfoReadFromDatabase: Collection<ClassInfo>) {
        val fileHelper: FileHelper = UtilCreator.INSTANCE.getDefaultInstance(FileHelper::class.java)

        classInfoReadFromDatabase.forEach { classInfo ->
            val content: String
            val finalAbsolutePath: String = absolutePathPrefixForDefault + classInfo.packageInfo.replace(".", ConstantParameters.FILE_SEPARATOR)

            when (classInfo.type) {
                ClassType.DEFAULT_CLASS -> {
                    content = JavaGenerator.getDefaultContent(configuration, classInfo).toString()
                    fileHelper.saveTextFile(
                        finalAbsolutePath,
                        classInfo.name,
                        ".java",
                        content
                    )
                }

                ClassType.ENUM -> {
                    content = JavaGenerator.getEnumContent(configuration, classInfo).toString()
                    fileHelper.saveTextFile(
                        finalAbsolutePath,
                        classInfo.name,
                        ".java",
                        content
                    )
                }

                else -> throw UtilRuntimeException(String.format("Unexpected ClassType(%s) of ClassInfo(%s).", classInfo.type.name, classInfo.name))
            }
        }
    }

    fun parseIntoClassInfo(column: MysqlColumnInfo): ClassInfo {
        var result: ClassInfo? = enumContainer[column.columnName]

        if (result != null) {
            return result
        } else {
            return when (column.dataType) {
                "tinyint" -> if (column.columnType == "tinyint(1)") {
                    // 实际上是 Boolean 类型
                    return ConstantClassInfoContainer.BOOLEAN
                } else {
                    ConstantClassInfoContainer.BYTE
                }

                "smallint" -> ConstantClassInfoContainer.SHORT
                "mediumint" -> ConstantClassInfoContainer.INTEGER
                "int" -> ConstantClassInfoContainer.INTEGER
                "bigint" -> ConstantClassInfoContainer.LONG
                "float" -> ConstantClassInfoContainer.FLOAT
                "double" -> ConstantClassInfoContainer.DOUBLE
                "decimal" -> ConstantClassInfoContainer.BIG_DECIMAL
                "datetime" -> configuration.defaultTimeType.classInfo
                "timestamp" -> configuration.defaultTimeType.classInfo
                "enum" -> {
                    var enumTypeRawMessage: String = column.columnType!!
                    // 删除开头的 "enum(" 与结尾的 ")"
                    enumTypeRawMessage = enumTypeRawMessage.substring(5, enumTypeRawMessage.length - 1)
                    // 删除 "'"
                    enumTypeRawMessage = enumTypeRawMessage.replace("'", "")
                    val enumElementArray: Array<String> = enumTypeRawMessage.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    var enumDescriptionRawMessage: String = column.columnComment!!
                    // 预期格式 →  类描述:枚举值1,枚举值2,……
                    val startPoint: Int = enumDescriptionRawMessage.indexOf(":")

                    // 类描述
                    var description: String? = null
                    if (startPoint >= 0) {
                        description = enumDescriptionRawMessage.substring(0, startPoint)
                    }

                    enumDescriptionRawMessage = enumDescriptionRawMessage.substring(startPoint + 1)
                    val enumElementDescriptionArray: Array<String> = enumDescriptionRawMessage.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    val enumElementList: MutableList<EnumElement> = ArrayList()

                    // 枚举值数目和枚举描述数是否匹配
                    val isEnumAndDescriptionMatch: Boolean = enumElementArray.size == enumElementDescriptionArray.size

                    // 初始化所有枚举值
                    enumElementArray.forEachIndexed { index, element ->
                        var description: String? = null
                        if (isEnumAndDescriptionMatch) {
                            description = enumElementDescriptionArray[index]
                        }

                        val enumValue = index * configuration.enumElementInterval

                        enumElementList.add(
                            EnumElement.builder()
                                .name(element)
                                .description(description)
                                .params(
                                    collectionHelper.createCollection(
                                        enumValue,
                                        element
                                    ) as List<Any>?
                                )
                                .build()
                        )
                    }

                    result = ClassInfo.builder()
                        .packageInfo(configuration.packageInfo + configuration.enumPathSuffix)
                        .description(description)
                        .name(parseIntoClassName(true, column.columnName!!) + configuration.enumNameSuffix)
                        .type(ClassType.ENUM)
                        .properties(
                            collectionHelper.createCollection(
                                Property.builder()
                                    .name("index")
                                    .classInfo(ConstantClassInfoContainer.INTEGER)
                                    .description("枚举序号值")
                                    .build(),
                                Property.builder()
                                    .name("text")
                                    .classInfo(ConstantClassInfoContainer.STRING)
                                    .description("枚举文本值")
                                    .build()
                            )
                        )
                        .enumElements(enumElementList)
                        .build()

                    result.init(configuration)

                    enumContainer[column.columnName!!] = result
                    return result
                }

                else -> ConstantClassInfoContainer.STRING
            }
        }
    }
}

