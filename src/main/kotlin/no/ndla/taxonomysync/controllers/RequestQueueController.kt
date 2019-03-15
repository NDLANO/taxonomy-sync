package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.services.RequestQueueService
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import no.ndla.taxonomysync.services.DynamoDbService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("requests")
class RequestQueueController(val dynamoDbService: DynamoDbService, val requestQueueService: RequestQueueService) {


    @GetMapping("/queue")
    fun getQueue():Array<TaxonomyApiRequest>{
        return dynamoDbService.getTaxonomyQueue()
    }

    @PostMapping("/queue")
    @ResponseBody
    fun enqueue(@RequestBody apiRequest: TaxonomyApiRequest): Int {
        return dynamoDbService.insertRequest(apiRequest)
    }

    @PostMapping("/process")
    fun process() {
        val taxonomyQueue = dynamoDbService.getTaxonomyQueue()
        taxonomyQueue.forEach(requestQueueService::add)
        while(true){
            if(requestQueueService.status.queuedItems == 0){
                dynamoDbService.deleteAllRequests()
                requestQueueService.stop()
                break
            }else{
                //Wait a bit 10 sec & Log stuff
            }

        }
    }

}