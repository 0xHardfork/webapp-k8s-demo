package com.night

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class Application {

    @Bean
    fun helloWorldRoute(): RouterFunction<ServerResponse> = router {
        GET("/hello") {
            ServerResponse.ok().bodyValue(HelloResponse("Hello, World!"))
        }
        
        GET("/hello/{name}") { request ->
            val name = request.pathVariable("name")
            ServerResponse.ok().bodyValue(HelloResponse("Hello, $name!"))
        }
        
        GET("/env") {
            val envMap = System.getenv().toMap()
            ServerResponse.ok().bodyValue(EnvironmentResponse(envMap))
        }
    }
}

data class HelloResponse(val message: String)
data class EnvironmentResponse(val environmentVariables: Map<String, String>)

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}