package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.dtos.CopyReport
import no.ndla.taxonomysync.services.DatabaseCopierService
import no.ndla.taxonomysync.services.DynamoDbService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("database")
class DatabaseCopierController(val databaseCopierService: DatabaseCopierService,
                               val dynamoDbService: DynamoDbService) {


    @PostMapping("/resetdraft")
    @ResponseBody
    fun resetDraftDatabase(): CopyReport {
        return databaseCopierService.copySourceToTarget()
    }

    @GetMapping("/insertmock")
    fun makeStuff():CopyReport{
        return dynamoDbService.createTable()
    }


}