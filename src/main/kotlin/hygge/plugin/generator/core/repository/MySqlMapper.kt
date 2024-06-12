package hygge.plugin.generator.core.repository

import hygge.plugin.generator.core.domain.po.MysqlColumnInfo
import hygge.plugin.generator.core.domain.po.MysqlTablesInfo
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select


@Mapper
interface MySqlMapper {
    /**
     * 查询 MySQL 中所有 SCHEMA 名称
     *
     * @param schema 库名
     * @return 表名集合
     */
    @Select("SELECT SCHEMA_NAME AS 'Database Name' FROM INFORMATION_SCHEMA.SCHEMATA")
    fun querySchemaNameList(): ArrayList<String>

    /**
     * 查询特定库中的所有表名
     *
     * @param schema 库名
     * @return 表名集合
     */
    @Select("SELECT * FROM information_schema.TABLES WHERE table_schema = #{schema} AND TABLE_TYPE= \"BASE TABLE\" ORDER BY TABLE_NAME ASC")
    fun queryTableInfoBySchemaName(@Param("schema") schema: String): ArrayList<MysqlTablesInfo>

    /**
     * 查询特定库中特定表全部字段的信息
     *
     * @param schema    库名
     * @param tableName 表名
     * @return 特定库中特定表全部字段的信息
     */
    @Select("SELECT *  FROM information_schema.COLUMNS  WHERE table_name = #{tableName}  AND table_schema = #{schema} ORDER BY ORDINAL_POSITION ASC")
    fun queryTableInfoBySchemaTableName(
        @Param("schema") schema: String,
        @Param("tableName") tableName: String
    ): ArrayList<MysqlColumnInfo>
}