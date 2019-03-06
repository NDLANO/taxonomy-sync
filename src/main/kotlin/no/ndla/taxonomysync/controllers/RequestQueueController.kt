package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.dtos.RequestQueueEnqueueResponse
import no.ndla.taxonomysync.dtos.TaxonomyRequest
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
    fun enqueue(request: TaxonomyRequest): RequestQueueEnqueueResponse {
        return RequestQueueEnqueueResponse()
    }

    @PostMapping("/process")
    fun process() {

    }


}