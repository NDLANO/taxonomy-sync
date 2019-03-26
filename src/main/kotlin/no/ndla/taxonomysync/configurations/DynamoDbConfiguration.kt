package no.ndla.taxonomysync.configurations

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "dynamodb")
class DynamoDbConfiguration {

    lateinit var server: String
    lateinit var region: String
    lateinit var tableName: String

    @Bean
    fun sourceDynamoDatabase(): DynamoDB {
        return DynamoDB(AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(server, region))
                .build())
    }
}