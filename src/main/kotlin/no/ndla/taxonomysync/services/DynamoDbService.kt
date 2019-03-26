package no.ndla.taxonomysync.services

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.*
import no.ndla.taxonomysync.configurations.DynamoDbConfiguration
import no.ndla.taxonomysync.dtos.CopyReport
import no.ndla.taxonomysync.dtos.TaxonomyApiRequest
import org.springframework.stereotype.Service
import java.util.*


@Service
class DynamoDbService(val sourceDynamoDatabase: DynamoDB, val config: DynamoDbConfiguration) {

    var table: Table = sourceDynamoDatabase.getTable(config.tableName)

    fun createTable(): CopyReport {
        val report = CopyReport()
        report.log.add("Database opprettet")
        report.log.add("Navn = ${config.tableName}")

        val attributeDefinitions = ArrayList<AttributeDefinition>()
        attributeDefinitions.add(AttributeDefinition().withAttributeName("Id").withAttributeType("S"))
        attributeDefinitions.add(AttributeDefinition().withAttributeName("timestamp").withAttributeType("S"))

        val keySchema = ArrayList<KeySchemaElement>()
        keySchema.add(KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH))
        keySchema.add(KeySchemaElement().withAttributeName("timestamp").withKeyType(KeyType.RANGE))

        val request = CreateTableRequest()
                .withTableName(config.tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L))

        val table = sourceDynamoDatabase.createTable(request)
        table.waitForActive()
        this.table = table
        return report
    }

    fun insertRequest(apiRequest: TaxonomyApiRequest): Int {
        val uuid: String = UUID.randomUUID().toString()
        val item = Item()
                .withPrimaryKey("Id", uuid, "timestamp", apiRequest.timestamp)
                .withString("timestamp", apiRequest.timestamp)
                .withString("body", apiRequest.body)
                .withString("path", apiRequest.path)
                .withString("method", apiRequest.method)
        return table.putItem(item).putItemResult.sdkHttpMetadata.httpStatusCode
    }

    fun getTaxonomyQueue(): Array<TaxonomyApiRequest> {
        var taxonomyQueue: Array<TaxonomyApiRequest> = arrayOf()
        table.scan().forEach {
            taxonomyQueue += TaxonomyApiRequest(
                    timestamp = it.get("timestamp") as String,
                    method = it.get("method") as String,
                    path = it.get("path") as String,
                    body = it.get("body") as String
            )
        }
        taxonomyQueue.sortBy { it.timestamp }
        return taxonomyQueue
    }

    fun resetTable() {
        table.delete()
        table.waitForDelete()
        createTable()
    }
}