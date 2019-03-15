package no.ndla.taxonomysync.services


import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.domain.RequestQueueStatus
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

import javax.annotation.PreDestroy
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


@Service
class RequestQueueService(config: RequestQueueConfiguration, private val requestPoster: TaxonomyApiRequestPoster) {
    private val waitTimeBetweenRetries: Long = config.waitTimeBetweenRetries
    private var autoEnqueueingRunning: Boolean = false
    private val requestQueue: BlockingQueue<TaxonomyApiRequest>
    private var currentRequest: TaxonomyApiRequest? = null
    private var currentAttemptCount = 0
    private lateinit var processingThread: Thread
    val status: RequestQueueStatus
        get() = RequestQueueStatus(currentRequest, currentAttemptCount, requestQueue.size)

    init {
        requestQueue = LinkedBlockingQueue<TaxonomyApiRequest>()
        startAutomaticEnqueuing()
    }


    fun add(request: TaxonomyApiRequest) {
        LOGGER.info("Adding taxonomy API request to local queue {}" + request.toString())
        requestQueue.add(request)
    }


    private fun startAutomaticEnqueuing() {
        autoEnqueueingRunning = true
        processingThread = Thread {

            while (autoEnqueueingRunning) {
                try {
                    if (currentRequest == null) {
                        currentRequest = requestQueue.take()
                    }
                    LOGGER.info("Attempting to enqueue request in sync queue: " + currentRequest!!.toString() + " (" + requestQueue.size + " items remaining in local queue")
                    try {
                        ++currentAttemptCount
                        val response = requestPoster.postTaxonomyRequestToProd(currentRequest!!)
                        if (response.statusCode.is2xxSuccessful) {
                            LOGGER.info("Sync queue insert success after $currentAttemptCount attempts")
                            currentRequest = null
                            currentAttemptCount = 0
                        } else {
                            LOGGER.error("Received non-success HTTP code ({}) when posting a Taxonomy API Request to sync, will retry in {} seconds", response.statusCode.value(), waitTimeBetweenRetries / 1000)
                            Thread.sleep(waitTimeBetweenRetries)
                        }
                    } catch (e: Exception) {
                        LOGGER.error("An error occurred when posting a Taxonomy API Request to sync, will retry in {} seconds", waitTimeBetweenRetries / 1000, e)
                        Thread.sleep(waitTimeBetweenRetries)
                    }

                } catch (e: InterruptedException) {
                    LOGGER.warn("Thread was interrupted, retrying", e)
                }

            }

        }
        processingThread.start()
    }


    @PreDestroy
    private fun preDestroy() {
        autoEnqueueingRunning = false
    }

    fun stop() {
        autoEnqueueingRunning = false
        processingThread.interrupt()

    }

    companion object {


        private val LOGGER = LoggerFactory.getLogger(RequestQueueService::class.java)
    }
}
