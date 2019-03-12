package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import no.ndla.taxonomysync.services.DynamoDbService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("requests")
class RequestQueueController(val dynamoDbService: DynamoDbService) {


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
    }

}