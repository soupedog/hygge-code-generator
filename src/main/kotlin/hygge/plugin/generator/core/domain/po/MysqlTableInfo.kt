package hygge.plugin.generator.core.domain.po

class MysqlTablesInfo {
    /**
     * 表所在库
     */
    var tableCatalog: String? = null
    var tableSchema: String? = null
    var tableName: String? = null
    var tableType: String? = null
    var engine: String? = null
    var version: Int? = null
    var rowFormat: String? = null
    var tableRows: Long? = null
    var avgRowLength: Long? = null
    var dataLength: Long? = null
    var maxDataLength: Long? = null
    var indexLength: Long? = null
    var dataFree: Long? = null
    var autoIncrement: Long? = null
    var createTime: String? = null
    var updateTime: String? = null
    var checkTime: String? = null
    var tableCollation: String? = null
    var checksum: String? = null
    var createOptions: String? = null
    var tableComment: String? = null
}