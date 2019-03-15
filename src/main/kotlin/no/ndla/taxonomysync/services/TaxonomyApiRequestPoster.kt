package no.ndla.taxonomysync.services

import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TaxonomyApiRequestPoster(config: RequestQueueConfiguration) {

    private val syncEndpoint: String = "http://" + config.targetHost
    private val restTemplate: RestTemplate = RestTemplate()


    fun postTaxonomyRequestToProd(request: TaxonomyApiRequest): ResponseEntity<String> {

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON

        return restTemplate.postForEntity(syncEndpoint + request.path, HttpEntity(request.body, httpHeaders), String::class.java)
    }

}
