package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.domain.EventLog
import no.ndla.taxonomysync.services.DatabaseCopierService
import no.ndla.taxonomysync.services.DynamoDbService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("database")
class DatabaseCopierController(val databaseCopierService: DatabaseCopierService,
                               val dynamoDbService: DynamoDbService,
                               val config: RequestQueueConfiguration
                               ) {


    @PostMapping("/resetdraft")
    @ResponseBody
    fun resetDraftDatabase(): EventLog {
        dynamoDbService.resetTable()
        return databaseCopierService.copySourceToTarget()
    }

}