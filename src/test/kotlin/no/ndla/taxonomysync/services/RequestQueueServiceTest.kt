package no.ndla.taxonomysync.services

import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.domain.TaxonomyApiRequest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.http.ResponseEntity

class RequestQueueServiceTest {

    lateinit var service: RequestQueueService
    lateinit var poster: TaxonomyApiRequestPoster

    @Before
    fun setUp() {
        var config = RequestQueueConfiguration()
        config.hostUrl = "nohost"
        config.waitTimeBetweenRetries = 10
        poster = Mockito.mock(TaxonomyApiRequestPoster::class.java)
        service = RequestQueueService(config, poster)
    }

    @Test
    fun emptyStatusOnStart() {
        val (currentRequest, currentAttempts, queuedItems) = service.status
        assertNull(currentRequest)
        assertEquals(0, currentAttempts)
        assertEquals(0, queuedItems)
    }

    @Test
    fun addingChangesQueuedItems() {
        service.add(TaxonomyApiRequest("2019-01-01T00:00:00.000", "PUT", "/v1/subjects", "{}"))
        service.add(TaxonomyApiRequest("2019-01-01T00:00:00.001", "PUT", "/v1/subjects", "{}"))
        val (currentRequest, currentAttempts, queuedItems) = service.status
        assertNull(currentRequest)
        assertEquals(0, currentAttempts)
        assertEquals(2, queuedItems)
    }

    @Test
    fun poisonPillDoesNotAffectQueueSize() {
        service.add(TaxonomyApiRequest("2019-01-01T00:00:00.000", "PUT", "/v1/subjects", "{}"))
        service.addPoisonPill()
        val (currentRequest, currentAttempts, queuedItems) = service.status
        assertNull(currentRequest)
        assertEquals(0, currentAttempts)
        assertEquals(1, queuedItems)
    }

    @Test
    fun queueProcessingCreatesRequests() {
        Mockito.`when`(poster.postTaxonomyRequestToProd(any()))
                .thenReturn(ResponseEntity.noContent().build())

        val taxonomyApiRequest = TaxonomyApiRequest(method = "POST", path = "/v1/dummy", body = "{}", timestamp = "2001-01-01T11:22:33:444")
        service.add(taxonomyApiRequest)
        service.startAutomaticEnqueuing()
        Thread.sleep(50L) //give blocking queue a chance to catch up
        Mockito.verify(poster, times(1)).postTaxonomyRequestToProd(taxonomyApiRequest)
    }

    @Test
    fun queueProcessingEndsWithPoisonPill() {
        Mockito.`when`(poster.postTaxonomyRequestToProd(any()))
                .thenReturn(ResponseEntity.noContent().build())

        val taxonomyApiRequest = TaxonomyApiRequest(method = "POST", path = "/v1/dummy", body = "{}", timestamp = "2001-01-01T11:22:33:444")
        service.add(taxonomyApiRequest)
        service.addPoisonPill()
        assertFalse(service.isProcessingThreadRunning())
        service.startAutomaticEnqueuing()
        Thread.sleep(50L) //give blocking queue a chance to catch up
        Mockito.verify(poster, times(1)).postTaxonomyRequestToProd(taxonomyApiRequest)
        assertFalse(service.isProcessingThreadRunning())
    }

    //these are needed to use Mockito.any() in Kotlin as of v1.3...
    //see https://medium.com/@elye.project/befriending-kotlin-and-mockito-1c2e7b0ef791
    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
}