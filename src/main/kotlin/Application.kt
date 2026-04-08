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
    }
}

data class HelloResponse(val message: String)

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}