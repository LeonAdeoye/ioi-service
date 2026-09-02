package com.leon

import com.leon.config.IoiProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(IoiProperties::class)
class IoiServiceApplication

fun main(args: Array<String>)
{
    runApplication<IoiServiceApplication>(*args)
}
