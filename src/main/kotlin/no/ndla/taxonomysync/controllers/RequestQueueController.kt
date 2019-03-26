package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.services.RequestQueueService
import no.ndla.taxonomysync.domain.TaxonomyApiRequest
import no.ndla.taxonomysync.services.DynamoDbService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("requests")
class RequestQueueController(val dynamoDbService: DynamoDbService, val requestQueueService: RequestQueueService) {

    var logger = LoggerFactory.getLogger(RequestQueueController::class.java)

    @GetMapping("/queue")
    fun getQueue():Array<TaxonomyApiRequest>{
        return dynamoDbService.getTaxonomyQueue()
    }

    @PostMapping("/queue")
    @ResponseBody
    fun enqueue(@RequestBody apiRequest: TaxonomyApiRequest): Int {
        logger.info("Inserting $apiRequest")
        return dynamoDbService.insertRequest(apiRequest)
    }

    @PostMapping("/process")
    fun process() {
        val taxonomyQueue = dynamoDbService.getTaxonomyQueue()
        requestQueueService.startQueueProcessing()
        taxonomyQueue.forEach(requestQueueService::add)
        requestQueueService.addPoisonPill()
        while(true){
            if(!requestQueueService.isProcessingThreadRunning()){
                logger.info("Queue empty, resetting table and stopping thread.")
                dynamoDbService.resetTable()
                break
            }else{
                logger.info("Waiting for queue processing to complete")
                Thread.sleep(1000)
            }
        }
    }

}