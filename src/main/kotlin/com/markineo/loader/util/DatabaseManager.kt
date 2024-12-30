package com.markineo.loader.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.logging.Logger

import kotlinx.coroutines.*
import java.sql.DriverManager

object DatabaseManager {
    private var logger: Logger? = null
    private lateinit var hikariDataSource: HikariDataSource

    fun setLogger(logger: Logger) {
        DatabaseManager.logger = logger
    }

    suspend fun setupDatabase(): Boolean {
        return try {
            val configFile = FileManager.databaseConfig

            val host = configFile?.getString("database.host")
            val user: String = configFile?.getString("database.user").orEmpty()
            val password: String = configFile?.getString("database.password").orEmpty()
            val database = configFile?.getString("database.database")
            val port = configFile?.getInt("database.port")
            val url = "jdbc:mysql://$host:$port/$database?characterEncoding=UTF-8"

            val hikariConfigured = configureHikariCP(url, user, password)
            if (!hikariConfigured) return false

            withContext(Dispatchers.IO) {
                getConnection().use { connection ->
                    createTable(connection)
                    logger?.info("Conectado com sucesso ao banco de dados MySQL.")
                }
            }
            true
        } catch (e: SQLException) {
            e.printStackTrace()
            logger?.severe(e.message)
            false
        }
    }

    private suspend fun configureHikariCP(url: String, user: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                DriverManager.registerDriver(com.mysql.cj.jdbc.Driver())

                val config = HikariConfig().apply {
                    jdbcUrl = url
                    username = user
                    this.password = password
                    minimumIdle = 5
                    maximumPoolSize = 80
                    idleTimeout = 60000
                    connectionTimeout = 30000
                    maxLifetime = 1800000
                    isAutoCommit = true
                }
                hikariDataSource = HikariDataSource(config)
                true
            } catch (e: Exception) {
                logger?.severe("Erro ao configurar o HikariCP: ${e.message}")
                false
            }
        }
    }

    @Throws(SQLException::class)
    fun getConnection(): Connection {
        var retries = 5
        while (retries > 0) {
            try {
                return hikariDataSource.connection
            } catch (e: SQLException) {
                logger?.info("Erro ao obter conexão: ${e.message}")
                Thread.sleep(1000)
                retries--
            }
        }
        throw SQLException("Falha ao obter conexão após várias tentativas.")
    }

    @Throws(SQLException::class)
    private fun createTable(connection: Connection) {
        connection.createStatement().use { statement ->
            // Tabela 'eb_blocks'
            val tableEbBlocks = """
            CREATE TABLE IF NOT EXISTS eb_blocks (
                loader_block_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                world_name VARCHAR(100) NOT NULL,
                position_serialized VARCHAR(100) NOT NULL UNIQUE,
                block_id VARCHAR(100) NOT NULL
            );
            """.trimIndent()
            statement.executeUpdate(tableEbBlocks)
        }
    }

    fun executeQueryRs(connection: Connection, sql: String, vararg params: Any): ResultSet? {
        return try {
            val statement = connection.prepareStatement(sql)
            params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
            statement.executeQuery()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    fun executeQuery(connection: Connection, sql: String, vararg parameters: Any) {
        try {
            connection.prepareStatement(sql).use { statement ->
                parameters.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
                statement.execute()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}