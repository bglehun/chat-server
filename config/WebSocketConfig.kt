package com.config

import com.common.exception.StompExceptionHandler
import com.socket.common.interceptor.*
import org.springframework.context.annotation.*
import org.springframework.messaging.simp.config.*
import org.springframework.scheduling.*
import org.springframework.scheduling.concurrent.*
import org.springframework.web.socket.config.annotation.*

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val webSocketInterceptor: WebSocketInterceptor,
    private val stompExceptionHandler: StompExceptionHandler,
) : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
        registry.setErrorHandler(stompExceptionHandler)
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/subscribe", "/match")
            .setTaskScheduler(heartBeatScheduler())
            .setHeartbeatValue(LongArray(2) { 30000L })
        registry.setApplicationDestinationPrefixes("/publish")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketInterceptor)
        registration.taskExecutor(
            ThreadPoolTaskExecutor().apply {
                corePoolSize = 40
//                maxPoolSize = 200
//                queueCapacity = 100000
//                setAllowCoreThreadTimeOut(true)
            },
        )
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.taskExecutor(
            ThreadPoolTaskExecutor().apply {
                corePoolSize = 10
            },
        )
    }

    @Bean
    fun heartBeatScheduler(): TaskScheduler {
        return ThreadPoolTaskScheduler()
    }
}
