package no.ndla.taxonomysync.services

import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.lang.RuntimeException
import java.net.URI

@Service
class TaxonomyApiRequestPoster(config: RequestQueueConfiguration) {

    private val syncEndpoint: String = "http://" + config.targetHost
    private val restTemplate: RestTemplate = RestTemplate()
    var logger = LoggerFactory.getLogger(TaxonomyApiRequestPoster::class.java)

    fun postTaxonomyRequestToProd(request: TaxonomyApiRequest): ResponseEntity<String> {

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        logger.info("Endpoint is $syncEndpoint${request.path}")
        logger.info("Method is ${request.method}")
        return restTemplate.exchange(URI(syncEndpoint + request.path), getMethod(request.method), HttpEntity(request.body, httpHeaders), String::class.java)
    }

    private fun getMethod(method: String): HttpMethod {
        return when (method) {
            "PUT" -> HttpMethod.PUT
            "POST" -> HttpMethod.POST
            "DELETE" -> HttpMethod.DELETE
            else -> throw RuntimeException("Unsupported method")
        }
    }

}
