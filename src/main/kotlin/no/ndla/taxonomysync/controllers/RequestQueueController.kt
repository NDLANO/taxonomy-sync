package no.ndla.taxonomysync.controllers

import com.amazonaws.services.dynamodbv2.document.Table
import no.ndla.taxonomysync.dtos.EnqueueResponse
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import no.ndla.taxonomysync.services.DynamoDbService
import java.util.HashSet
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct


@RestController
@RequestMapping("requests")
class RequestQueueController(val dynamoDbService: DynamoDbService) {


    @GetMapping("/queue")
    fun getQueue():Array<String>{
        return arrayOf("Hello", "World")
    }

    @PostMapping("/queue")
    @ResponseBody
    fun enqueue(@RequestBody apiRequest: TaxonomyApiRequest): PutItemOutcome {
        return dynamoDbService.getOutcome(apiRequest)
    }

    @PostMapping("/process")
    fun process() {

    }

}