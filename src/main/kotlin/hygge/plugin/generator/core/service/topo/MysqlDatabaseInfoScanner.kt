package hygge.plugin.generator.core.service.topo

import hygge.plugin.generator.core.domain.po.MysqlColumnInfo
import hygge.plugin.generator.core.domain.po.MysqlTablesInfo
import hygge.plugin.generator.core.repository.MySqlMapper
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.TransactionFactory
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import javax.sql.DataSource

class MysqlDatabaseInfoScanner(hostPort: String, userName: String, password: String) {
    private var sqlSessionFactory: SqlSessionFactory

    init {
        sqlSessionFactory = createSqlSessionFactory(createDataSource(hostPort, userName, password))
    }

    fun getSchemaNameList(): ArrayList<String> {
        val sqlSession: SqlSession = sqlSessionFactory.openSession()
        val mapper: MySqlMapper = sqlSession.getMapper(MySqlMapper::class.java)

        sqlSession.use {
            return mapper.querySchemaNameList()
        }
    }

    fun getTableInfoBySchemaName(schema: String): ArrayList<MysqlTablesInfo> {
        val sqlSession: SqlSession = sqlSessionFactory.openSession()
        val mapper: MySqlMapper = sqlSession.getMapper(MySqlMapper::class.java)

        sqlSession.use {
            return mapper.queryTableInfoBySchemaName(schema)
        }
    }

    fun getColumnInfoBySchemaTableName(
        schema: String,
        tableName: String
    ): ArrayList<MysqlColumnInfo> {
        val sqlSession: SqlSession = sqlSessionFactory.openSession()
        val mapper: MySqlMapper = sqlSession.getMapper(MySqlMapper::class.java)

        sqlSession.use {
            return mapper.queryTableInfoBySchemaTableName(schema, tableName)
        }
    }

    private fun createDataSource(hostPort: String, userName: String, password: String): UnpooledDataSource {
        val dataSource = UnpooledDataSource()
        dataSource.driver = "com.mysql.cj.jdbc.Driver"
        dataSource.url = "jdbc:mysql://$hostPort?serverTimezone=UTC&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true"
        dataSource.username = userName
        dataSource.password = password
        dataSource.defaultNetworkTimeout = 60000
        return dataSource
    }

    private fun createSqlSessionFactory(dataSource: DataSource): SqlSessionFactory {
        /*
         * 更多内容 → https://mybatis.org/mybatis-3/zh_CN/getting-started.html
         */
        val transactionFactory: TransactionFactory = JdbcTransactionFactory()
        val environment = Environment("development", transactionFactory, dataSource)
        val configuration = Configuration(environment)

        // 开启驼峰下划线转化  AUTO_INCREMENT(表字段名) → autoIncrement(PO 属性名)
        configuration.isMapUnderscoreToCamelCase = true
        //加入一个映射器
        configuration.addMapper(MySqlMapper::class.java)
        //使用 SqlSessionFactoryBuilder 构建 SqlSessionFactory
        return SqlSessionFactoryBuilder().build(configuration)
    }
}