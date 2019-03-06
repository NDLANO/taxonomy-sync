package no.ndla.taxonomysync.configurations

import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import javax.sql.DataSource

@Configuration
class PostgresInstancesConfiguration(val env: Environment) {

    @Bean
    @Qualifier("source")
    fun sourceDatabase(): DataSource {
        val dataSource = PGSimpleDataSource()
        dataSource.portNumber = 5432
        dataSource.serverName = env.getRequiredProperty("source_server")
        dataSource.databaseName = env.getRequiredProperty("source_db")
        dataSource.user = env.getRequiredProperty("source_user")
        dataSource.password = env.getRequiredProperty("source_pw")
        return dataSource
    }

    @Bean
    @Qualifier("target")
    fun targetDatabase(): DataSource {
        val dataSource = PGSimpleDataSource()
        dataSource.portNumber = 5432
        dataSource.serverName = env.getRequiredProperty("target_server")
        dataSource.databaseName = env.getRequiredProperty("target_db")
        dataSource.user = env.getRequiredProperty("target_user")
        dataSource.password = env.getRequiredProperty("target_pw")
        return dataSource
    }


}