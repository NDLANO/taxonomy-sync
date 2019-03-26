package no.ndla.taxonomysync.controllers

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
    @ApiOperation(value = "Get the current draft queue")
    fun getQueue():Array<TaxonomyApiRequest>{
        return dynamoDbService.getTaxonomyQueue()
    }

    @PostMapping("/queue")
    @ApiOperation(value = "Create a new queue element")
    @ResponseBody
    fun enqueue(@ApiParam(name = "API Request", value = "The new api request to be added to queue") @RequestBody apiRequest: TaxonomyApiRequest): Int {
        logger.info("Inserting $apiRequest")
        return dynamoDbService.insertRequest(apiRequest)
    }

    @PostMapping("/process")
    @ApiOperation(value = "Process current draft queue", notes = "This will post to selected target environment")
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