package no.ndla.taxonomysync.services

import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.domain.Authentication
import no.ndla.taxonomysync.domain.AuthenticationRequest
import no.ndla.taxonomysync.domain.TaxonomyApiRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.lang.RuntimeException
import java.net.URI
import java.time.Instant

@Service
class TaxonomyApiRequestSender(val config: RequestQueueConfiguration) {

    private val syncEndpoint: String = "http://" + config.hostUrl
    private val restTemplate: RestTemplate = RestTemplate()

    fun sendRequestToTargetHost(request: TaxonomyApiRequest): ResponseEntity<String> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        if (config.clientId != "ITEST") {
            getAccessToken(config.clientId, config.clientSecret, config.tokenServer)
            httpHeaders.run {
                set("Authorization", "Bearer ${config.authentication!!.access_token}")
            }
        }
        LOGGER.info("Endpoint is $syncEndpoint${request.path}")
        LOGGER.info("Method is ${request.method}")
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

    private fun getAccessToken(clientId: String, clientSecret: String, token_server: String) {
        if(shouldUpdateToken(config.lastTokenUpdate, config.authentication)){

            val cmd = AuthenticationRequest(clientId, clientSecret)
            val request:HttpEntity<AuthenticationRequest> = HttpEntity(cmd)
            val response: ResponseEntity<Authentication>?
            try {
                response = restTemplate.exchange(token_server, HttpMethod.POST, request, Authentication::class.java)
                config.authentication = response.body!!
                config.lastTokenUpdate = Instant.now().toEpochMilli()
            } catch (e: IllegalStateException) {
                println("401 Wrong Credentials? You are using the environment: $token_server")
            } catch (e: HttpClientErrorException) {
                println("401 Wrong Credentials? You are using the environment: $token_server")
            }
        }

    }

    private fun shouldUpdateToken(lastTokenUpdate: Long?, authentication: Authentication?): Boolean {
        return (lastTokenUpdate == null
                || authentication == null
                || lastTokenUpdate + (authentication.expires_in - 300) * 1000 <= Instant.now().toEpochMilli())
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(TaxonomyApiRequestSender::class.java)
    }

}
