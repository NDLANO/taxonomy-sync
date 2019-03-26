package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.domain.EventLog
import io.swagger.annotations.ApiOperation
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
    fun resetDraftDatabase(): EventLog {
        dynamoDbService.resetTable()
        return databaseCopierService.copySourceToTarget()
    }



}