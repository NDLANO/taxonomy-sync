package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.configurations.RequestQueueConfiguration
import no.ndla.taxonomysync.dtos.CopyReport
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
    fun resetDraftDatabase(): CopyReport {
        dynamoDbService.resetTable()
        return databaseCopierService.copySourceToTarget()
    }

    @GetMapping("/insertmock")
    fun makeStuff():CopyReport{
        //TODO remove after everything works. This is only for mocking purposes.
        return dynamoDbService.createTable()
    }

    @GetMapping("/test")
    fun configTest() {
        //TODO remove after everything works. This is only for mocking purposes.
        val waitTimeBetweenRetries: Long = config.waitTimeBetweenRetries
        println("Connection to config - should be (300000), is: $waitTimeBetweenRetries")
    }

}