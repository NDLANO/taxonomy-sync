package no.ndla.taxonomysync.services

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.TableCollection
import com.amazonaws.services.dynamodbv2.model.*
import no.ndla.taxonomysync.configurations.DynamoDbConfiguration
import no.ndla.taxonomysync.domain.EventLog
import no.ndla.taxonomysync.domain.TaxonomyApiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct


@Service
class DynamoDbService(val sourceDynamoDatabase: DynamoDB, val config: DynamoDbConfiguration) {

    lateinit var table: Table

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DynamoDbService::class.java)
    }

    @PostConstruct
    private fun init() {
        val tables: TableCollection<ListTablesResult> = sourceDynamoDatabase.listTables()
        var exists = false
        for(table: Table in tables){
            if(table.tableName == config.tableName){
                exists = true
                this.table = table
                break
            }
        }
        if(!exists){
            createTable()
        }
    }

    private fun createTable(): Table {
        LOGGER.info("Database opprettet, Navn: ${config.tableName}")
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
        return table
    }

    fun insertRequest(apiRequest: TaxonomyApiRequest): Int {
        val uuid: String = UUID.randomUUID().toString()
        val item = Item()
                .withPrimaryKey("Id", uuid, "timestamp", apiRequest.timestamp)
                .withString("timestamp", apiRequest.timestamp)
                .withString("body", apiRequest.body!!)
                .withString("path", apiRequest.path)
                .withString("method", apiRequest.method)
        return table.putItem(item).putItemResult.sdkHttpMetadata.httpStatusCode
    }

    fun getTaxonomyQueue(): Array<TaxonomyApiRequest> {
        var taxonomyQueue: Array<TaxonomyApiRequest> = arrayOf()
        table.scan().forEach { item: Item ->
            taxonomyQueue += with(item) {
                TaxonomyApiRequest(getString("timestamp"), getString("method"), getString("path"), getString("body"))
            }
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