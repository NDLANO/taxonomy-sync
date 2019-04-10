package no.ndla.taxonomysync

import no.ndla.taxonomysync.configurations.DynamoDbConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class TaxonomySyncApplication

fun main(args: Array<String>) {
    runApplication<TaxonomySyncApplication>(*args)
}
