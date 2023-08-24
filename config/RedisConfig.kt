package com.aimed.signalschat.config

import com.aimed.signalschat.entity.jpa.DmMessage
import com.aimed.signalschat.socket.handler.RedisSubscriber
import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.util.*

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.cluster-nodes}") private val redisNodes: List<String>,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun redisClusterConnectionFactory(): RedisConnectionFactory {
        val clusterConfiguration = RedisClusterConfiguration(redisNodes)

        // failover를 위한 topology 자동 업데이트 옵션, default 60s
        val clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enableAllAdaptiveRefreshTriggers()
            .build()
        val clientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(clusterTopologyRefreshOptions)
            .build()

        val lettuceClientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(10))
            .clientOptions(clientOptions)
            .build()

        return LettuceConnectionFactory(clusterConfiguration, lettuceClientConfig)
    }

    @Bean(name = ["redisTemplateForDmMessage"])
    fun redisTemplateForDmMessage(): RedisTemplate<String, DmMessage> {
        return RedisTemplate<String, DmMessage>().apply {
            connectionFactory = redisClusterConnectionFactory()
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(objectMapper, DmMessage::class.java)
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }
    }

    @Bean(name = ["redisTemplateForString"])
    fun redisTemplateForString(): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            connectionFactory = redisClusterConnectionFactory()
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }
    }

    @Bean
    fun redisMessageListenerContainer(
        listenerAdapter: MessageListenerAdapter,
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(redisClusterConnectionFactory())
            addMessageListener(
                listenerAdapter,
                ChannelTopic(UUID.randomUUID().toString()),
            )
        }
    }

    @Bean
    fun listenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber)
    }
}
