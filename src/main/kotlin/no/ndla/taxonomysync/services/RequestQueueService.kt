package no.ndla.taxonomysync.services


import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.domain.RequestQueueStatus
import no.ndla.taxonomysync.domain.PoisonPill
import no.ndla.taxonomysync.domain.Queueable
import no.ndla.taxonomysync.domain.TaxonomyApiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PreDestroy
import kotlin.concurrent.thread


@Service
class RequestQueueService(config: RequestQueueConfiguration, private val requestPoster: TaxonomyApiRequestPoster) {
    private val waitTimeBetweenRetries: Long = config.waitTimeBetweenRetries
    private var autoEnqueueingRunning: Boolean = false
    private val requestQueue: BlockingQueue<Queueable>
    private var currentQueueItem: Queueable? = null
    private var currentAttemptCount = 0
    private var processingThread: Thread? = null

    val status: RequestQueueStatus
        get() = RequestQueueStatus(if (currentQueueItem is TaxonomyApiRequest) currentQueueItem as TaxonomyApiRequest
        else null, currentAttemptCount, getRequestQueueSizeExcludingPoisonPills())

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RequestQueueService::class.java)
    }

    init {
        requestQueue = LinkedBlockingQueue<Queueable>()
    }

    fun add(request: TaxonomyApiRequest) {
        LOGGER.info("Adding taxonomy API request to local queue {} $request")
        requestQueue.add(request)
    }

    @Synchronized
    fun startAutomaticEnqueuing() {
        if (processingThread == null || processingThread!!.isAlive) {
            autoEnqueueingRunning = true
            processingThread = thread(start = true) {
                checkQueue@
                while (autoEnqueueingRunning) {
                    try {
                        if (currentQueueItem == null) {
                            currentQueueItem = requestQueue.take()
                            if (currentQueueItem is PoisonPill) {
                                autoEnqueueingRunning = false
                                continue@checkQueue
                            }
                        }
                        LOGGER.info("Attempting to enqueue request in sync queue: " + currentQueueItem!!.toString() + " (" + requestQueue.size + " items remaining in local queue")
                        ++currentAttemptCount
                        val response = requestPoster.postTaxonomyRequestToProd(currentQueueItem!! as TaxonomyApiRequest)
                        if (response.statusCode.is2xxSuccessful) {
                            LOGGER.info("Sync queue insert success after $currentAttemptCount attempts")
                            currentQueueItem = null
                            currentAttemptCount = 0
                        } else {
                            LOGGER.error("Received non-success HTTP code ({}) when posting a Taxonomy API Request to sync, will retry in {} seconds", response.statusCode.value(), waitTimeBetweenRetries / 1000)
                            Thread.sleep(waitTimeBetweenRetries)
                        }
                    } catch (e: InterruptedException) {
                        LOGGER.warn("Thread was interrupted, retrying", e)
                    } catch (e: Exception) {
                        LOGGER.error("An error occurred when posting a Taxonomy API Request to sync, will retry in {} seconds", waitTimeBetweenRetries / 1000, e)
                        Thread.sleep(waitTimeBetweenRetries)
                    }
                }
                LOGGER.info("Queue processing thread ends")
            }

        } else {
            LOGGER.info("Processing thread is already running, ignoring start request.")
        }
    }

    fun isProcessingThreadRunning() :Boolean = processingThread != null && processingThread!!.isAlive

    private fun getRequestQueueSizeExcludingPoisonPills(): Int {
        return requestQueue.count { it is TaxonomyApiRequest }
    }

    @PreDestroy
    private fun preDestroy() {
        addPoisonPill()
    }

    fun addPoisonPill() {
        requestQueue.add(PoisonPill())

    }
}

