package no.ndla.taxonomysync.services


import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.domain.PoisonPill
import no.ndla.taxonomysync.domain.Queueable
import no.ndla.taxonomysync.domain.RequestQueueStatus
import no.ndla.taxonomysync.domain.TaxonomyApiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PreDestroy
import kotlin.concurrent.thread


@Service
class RequestQueueService(config: RequestQueueConfiguration, private val requestSender: TaxonomyApiRequestSender) {
    private val waitTimeBetweenRetries: Long = config.waitTimeBetweenRetries
    var processing: Boolean = false
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
    fun startQueueProcessing() {
        LOGGER.info("Starting queue processing");
        if (processingThread == null || !(processingThread!!.isAlive)) {
            processing = true
            processingThread = thread(start = true) {
                checkQueue@
                while (processing) {
                    try {
                        if (currentQueueItem == null) {
                            currentQueueItem = requestQueue.take()
                        }
                        if (currentQueueItem is PoisonPill) {
                            LOGGER.info("PoisonPill encountered, ending queue processing")
                            processing = false
                            currentQueueItem = null
                            continue@checkQueue
                        } else {
                            LOGGER.info("Attempting to post request to target host: ${currentQueueItem!!} - (${getRequestQueueSizeExcludingPoisonPills()} items remaining in local queue")
                            ++currentAttemptCount
                            val response = requestSender.sendRequestToTargetHost(currentQueueItem!! as TaxonomyApiRequest)
                            if (response.statusCode.is2xxSuccessful) {
                                LOGGER.info("Sync queue insert success after $currentAttemptCount attempts")
                                currentQueueItem = null
                                currentAttemptCount = 0
                            } else {
                                LOGGER.error("Received non-success HTTP code (${response.statusCode.value()}) when posting a Taxonomy API Request to sync, will retry in ${waitTimeBetweenRetries / 1000} seconds")
                                Thread.sleep(waitTimeBetweenRetries)
                            }
                        }
                    } catch (e: InterruptedException) {
                        LOGGER.warn("Thread was interrupted - stopping processing", e)
                        processing = false
                        currentQueueItem = null
                        continue@checkQueue
                    } catch (e: Exception) {
                        LOGGER.error("An error occurred when posting a Taxonomy API Request to sync, will retry in ${waitTimeBetweenRetries / 1000} seconds", e)
                        Thread.sleep(waitTimeBetweenRetries)
                    }
                }
                LOGGER.info("Queue processing thread ends")
            }
            processingThread = null
            currentQueueItem = null
        } else {
            LOGGER.info("Processing thread is already running, ignoring start request.")
        }
    }

    private fun getRequestQueueSizeExcludingPoisonPills(): Int {
        return requestQueue.count { it is TaxonomyApiRequest }
    }

    fun addPoisonPill() {
        requestQueue.add(PoisonPill())
    }
}

