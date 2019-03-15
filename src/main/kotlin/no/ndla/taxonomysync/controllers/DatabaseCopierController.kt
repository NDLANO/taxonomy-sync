package no.ndla.taxonomysync.controllers

import no.ndla.taxonomysync.dtos.CopyReport
import no.ndla.taxonomysync.services.DatabaseCopierService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody


@Controller("database")
class DatabaseCopierController(val databaseCopierService: DatabaseCopierService) {


    @PostMapping("/resetdraft")
    @ResponseBody
    fun resetDraftDatabase(): CopyReport {
        return databaseCopierService.copySourceToTarget()
    }

}