package no.ndla.taxonomysync.configurations

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.ArrayList
import javax.annotation.PostConstruct

@Configuration
class DynamoDbConfiguration(val env: Environment) {

    @Bean
    fun sourceDynamoDatabase(): DynamoDB{
        val client = AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(env.getRequiredProperty("queue_server"),
                        env.getRequiredProperty("queue_region"))
                ).build()
        return DynamoDB(client)
    }
}