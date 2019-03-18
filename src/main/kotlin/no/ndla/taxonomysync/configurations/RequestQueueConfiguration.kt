package no.ndla.taxonomysync.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "target-queue")
class RequestQueueConfiguration {
    lateinit var hostUrl: String
    var waitTimeBetweenRetries: Long = 0
}