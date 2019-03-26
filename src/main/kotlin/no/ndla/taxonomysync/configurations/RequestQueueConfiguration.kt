package no.ndla.taxonomysync.configurations

import no.ndla.taxonomysync.domain.Authentication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "target-queue")
class RequestQueueConfiguration {
    lateinit var hostUrl: String
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var tokenServer: String
    var authentication: Authentication? = null
    var lastTokenUpdate: Long? = null
    var waitTimeBetweenRetries: Long = 0
}