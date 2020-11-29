/*
 * Copyright 2020 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia

import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.layout.TTLLLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import com.monkopedia.LoggingLevel.DEBUG
import com.monkopedia.LoggingLevel.ERROR
import com.monkopedia.LoggingLevel.FATAL
import com.monkopedia.LoggingLevel.INFO
import com.monkopedia.LoggingLevel.WARN
import java.io.File
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineExceptionHandler
import org.slf4j.LoggerFactory

enum class LoggingLevel(val prefix: String) {
    INFO(""),
    DEBUG("D: "),
    WARN("W: "),
    ERROR("E: "),
    FATAL("FATAL!!!: ")
}

fun Logger.info(str: String) = log(INFO, str)
fun Logger.debug(str: String) = log(DEBUG, str)
fun Logger.warn(str: String) = log(WARN, str)
fun Logger.error(str: String) = log(ERROR, str)
fun Logger.fatal(str: String) = log(FATAL, str)

interface Logger {
    fun log(level: LoggingLevel, str: String)
}

object MissingLogger : Logger {
    override fun log(level: LoggingLevel, str: String) {
        throw NotImplementedError("Logging not initialized")
    }
}

object Log : Logger {
    private var logger: Logger = MissingLogger

    fun init(logger: Logger) {
        Log.logger = logger
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val rootLogger =
            context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
        rootLogger.detachAndStopAllAppenders()
        when (logger) {
            StdoutLogger -> {
                BasicConfigurator().configure(context)
            }
            is FileLogger -> {
                val ca = FileAppender<ILoggingEvent>()
                ca.context = context
                ca.name = "file"
                ca.file = logger.file.absolutePath
                val encoder = LayoutWrappingEncoder<ILoggingEvent>()
                encoder.context = context

                val layout = TTLLLayout()
                layout.context = context
                layout.start()
                encoder.layout = layout

                ca.encoder = encoder
                ca.start()

                rootLogger.addAppender(ca)
            }
            else -> {
                throw IllegalArgumentException("Unexpected logger $logger")
            }
        }
    }

    override fun log(level: LoggingLevel, str: String) = logger.log(level, str)

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        fatal(throwable.stackTraceToString())
    }
}

object StdoutLogger : Logger {

    val logger by lazy {
        LoggerFactory.getLogger("General")
    }

    override fun log(level: LoggingLevel, str: String) {
        when (level) {
            INFO -> logger.info(str)
            DEBUG -> logger.debug(str)
            WARN -> logger.warn(str)
            ERROR -> logger.error(str)
            FATAL -> logger.error(str)
        }
        if (level == FATAL) {
            exitProcess(1)
        }
    }
}

class FileLogger(val file: File) : Logger {
    // private val loggingThread = Executors.newSingleThreadExecutor()
    // val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    val logger by lazy {
        LoggerFactory.getLogger("General")
    }

    override fun log(level: LoggingLevel, str: String) {
        when (level) {
            INFO -> logger.info(str)
            DEBUG -> logger.debug(str)
            WARN -> logger.warn(str)
            ERROR -> logger.error(str)
            FATAL -> logger.error(str)
        }
        if (level == FATAL) {
            exitProcess(1)
        }
    }
}
