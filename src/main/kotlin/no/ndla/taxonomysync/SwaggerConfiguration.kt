package no.ndla.taxonomysync

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

import java.net.URI
import java.util.Arrays

import com.google.common.collect.Sets.newHashSet
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8

@Configuration
@EnableSwagger2
class SwaggerConfiguration {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("no.ndla.taxonomysync.controllers"))
                .paths(PathSelectors.regex("/.*"))
                .build()
                .pathMapping("/")
                .apiInfo(apiInfo())
                .directModelSubstitute(URI::class.java, String::class.java)
                .directModelSubstitute(Array<URI>::class.java, Array<String>::class.java)
                .securitySchemes(listOf(apiKey()))
                .securityContexts(listOf(securityContext()))
                .useDefaultResponseMessages(true)
                .produces(newHashSet(APPLICATION_JSON_UTF8.toString()))
                .consumes(newHashSet(APPLICATION_JSON_UTF8.toString()))

    }

    private fun apiKey(): ApiKey {
        return ApiKey("apiKey", "Authorization", "header")
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/.*"))
                .build()
    }


    private fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return Arrays.asList<SecurityReference>(SecurityReference("apiKey", authorizationScopes))
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfo(
                "NDLA Taxonomy Sync",
                "Rest service for editing content.\n\n" +
                        "Unless otherwise specified, all PUT and POST requests must use " +
                        "Content-Type: application/json;charset=UTF-8. If charset is omitted, UTF-8 will be assumed. " +
                        "All GET requests will return data using the same content type.\n\n" +
                        "If you are using Swagger in an environment that requires authentication you will need a valid " +
                        "JWT token to POST the /process. Apply this by typing 'Bearer [YOUR TOKEN]' in the 'Authorize' dialog",
                "v1",
                null, null,
                "GPL 3.0",
                "https://www.gnu.org/licenses/gpl-3.0.en.html",
                kotlin.collections.emptyList()
        )
    }

}
