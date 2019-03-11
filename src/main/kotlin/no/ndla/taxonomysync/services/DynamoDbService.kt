package no.ndla.taxonomysync.services

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.*
import no.ndla.taxonomysync.dtos.CopyReport
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import org.springframework.stereotype.Service

@Service
class DynamoDbService(val sourceDynamoDatabase: DynamoDB) {

    fun createTable(tableName: String): CopyReport {
        val report = CopyReport()
        report.log.add("Database opprettet")
        report.log.add("Navn = $tableName")


        val attributeDefinitions = ArrayList<AttributeDefinition>()
        attributeDefinitions.add(AttributeDefinition().withAttributeName("Id").withAttributeType("N"))

        val keySchema = ArrayList<KeySchemaElement>()
        keySchema.add(KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH))

        val request = CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L))

        val table = sourceDynamoDatabase.createTable(request)

        table.waitForActive()

        return report
    }

    fun getOutcome(apiRequest: TaxonomyApiRequest): PutItemOutcome {
        var test: Table = sourceDynamoDatabase.getTable("taxosync")

        println(apiRequest?.method)
        println(apiRequest?.path)
        println(apiRequest?.body)
        println(apiRequest.method)
        println(apiRequest.path)
        println(apiRequest.body)

        val item = Item()
                .withPrimaryKey("Id", 1233455)
                .withString("body", apiRequest.body)
                .withString("path", apiRequest.path)
                .withString("method", apiRequest.method)

        return test.putItem(item)
    }

}