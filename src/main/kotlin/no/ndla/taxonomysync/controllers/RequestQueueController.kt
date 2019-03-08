package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.dtos.EnqueueResponse
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("requests")
class RequestQueueController {


    @GetMapping("/queue")
    fun getQueue():Array<String>{
        return arrayOf("Hello", "World")
    }

    @PostMapping("/queue")
    fun enqueue(apiRequest: TaxonomyApiRequest): EnqueueResponse {
        return EnqueueResponse()
    }

    @PostMapping("/process")
    fun process() {

    }


}