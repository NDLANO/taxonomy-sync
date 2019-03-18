package no.ndla.taxonomysync.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "requestqueue")
class RequestQueueConfiguration {
    var enabled: Boolean = false
    lateinit var targetHost: String
    var waitTimeBetweenRetries: Long = 0
}