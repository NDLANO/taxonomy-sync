package no.ndla.taxonomysync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaxonomySyncApplication

fun main(args: Array<String>) {
    runApplication<TaxonomySyncApplication>(*args)
}
