package no.ndla.taxonomysync.configurations

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.ArrayList
import javax.annotation.PostConstruct

@Configuration
@ConfigurationProperties(prefix = "requestqueue")
class RequestQueueConfiguration() {

    var enabled: Boolean = false
    lateinit var targetHost: String
    var waitTimeBetweenRetries: Long = 0;
}

/*
myconfig:
  my-host: ssl://example.com
  my-port: 23894
  my-user: user
  my-pass: pass

---------------------

@Configuration
@ConfigurationProperties(prefix = "myconfig")
class MqttProperties {
    lateinit var myHost: String
    lateinit var myPort: String
    lateinit var myUser: String
    lateinit var myPass: String
}*/