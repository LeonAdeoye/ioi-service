package com.leon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IoiServiceApplication

fun main(args: Array<String>)
{
    runApplication<IoiServiceApplication>(*args)
}