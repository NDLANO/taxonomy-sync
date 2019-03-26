package no.ndla.taxonomysync.controllers

import io.swagger.annotations.ApiOperation
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
    @ApiOperation(value = "Resets the draft", notes = "Only use when you want to delete everything. This will overwrite the draft db with the target environment db")
    fun resetDraftDatabase(): CopyReport {
        dynamoDbService.resetTable()
        return databaseCopierService.copySourceToTarget()
    }

    @GetMapping("/insertmock")
    @ApiOperation(value = "Insert a mock", notes = "Used for testing purposes.")
    fun makeStuff():CopyReport{
        //TODO remove after everything works. This is only for mocking purposes.
        return dynamoDbService.createTable()
    }

}