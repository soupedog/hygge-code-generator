package hygge.plugin.generator.core.domain.po

class MysqlColumnInfo {
    /**
     * 表限定符
     */
    var tableCatalog: String? = null

    /**
     * 表所在库
     */
    var tableSchema: String? = null

    /**
     * 表名
     */
    var tableName: String? = null

    /**
     * 列名(表字段名)
     */
    var columnName: String? = null

    /**
     * 列标识号
     */
    var ordinalPosition: Int? = null

    /**
     * 字段默认值
     */
    var columnDefault: String? = null

    /**
     * 是否可空
     */
    var isNullable: String? = null

    /**
     * 字段类型
     */
    var dataType: String? = null

    /**
     * 字段形式的最大值长度
     */
    var characterMaximumLength: Long? = null

    /**
     * 字节形式最大长度
     */
    var characterOctetLength: Long? = null
    var numericPrecision: Int? = null
    var numericScale: Int? = null
    var datetimePrecision: Int? = null

    /**
     * 字符编码集合名称
     */
    var characterSetName: String? = null

    /**
     * 字符编码名称
     */
    var collationName: String? = null
    var columnType: String? = null

    /**
     * PRI 主键,UNI 唯一键
     */
    var columnKey: String? = null
    var extra: String? = null
    var privileges: String? = null

    /**
     * 字段备注
     */
    var columnComment: String? = null
    var generationExpression: String? = null

    fun isPK(): Boolean {
        return "PRI" == columnKey
    }
}