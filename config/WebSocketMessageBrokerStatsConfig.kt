package com.aimed.signalschat.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.WebSocketMessageBrokerStats

@Configuration
class WebSocketMessageBrokerStatsConfig(
    private val webSocketMessageBrokerStats: WebSocketMessageBrokerStats,
) {
    @PostConstruct
    fun init() {
        // 메시지 브로커 상태 로깅 간격 설정
        webSocketMessageBrokerStats.loggingPeriod = 2 * 60 * 1000
    }
}
