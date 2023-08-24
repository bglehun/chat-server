package com.aimed.signalschat.entity.jpa

import com.aimed.signalschat.api.dto.DmDto
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "dm_message",
    indexes = [
        Index(
            name = "idx_unique_room_id_message_id_created_at",
            columnList = "roomId DESC, messageId DESC, createdAt DESC",
            unique = true,
        ),
    ],
)
class DmMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val messageId: Long? = null,

    @Column(nullable = false, length = 64)
    val roomId: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: MessageType = MessageType.MESSAGE,

    // 메세지 수정 기능이 존재해야 할지?
    @Column(nullable = false, columnDefinition = "TEXT", length = 200)
    var message: String,

    @Column(nullable = false, length = 36)
    val senderId: String,

    @Column(nullable = false)
    val receiverId: String,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    enum class MessageType {
        MESSAGE, // 이모티콘, 시스템, 등이 들어갈 것으로 예상
    }

    companion object {
        fun from(room: DmRoom, message: DmMessage?): DmDto.DmRoomsResponse {
            return DmDto.DmRoomsResponse(room.id, message?.message, message?.type, message?.createdAt)
        }
    }
}
