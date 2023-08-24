package com.socket.dto

import com.entity.jpa.DmMessage

class DmMessageDto(
    val roomId: String,
    val message: String,
    val type: DmMessage.MessageType,
    var senderId: String,
    val receiverId: String,
) {
    companion object {
        fun toEntity(dto: DmMessageDto) =
            DmMessage(
                roomId = dto.roomId,
                message = dto.message,
                type = dto.type,
                senderId = dto.senderId,
                receiverId = dto.receiverId,
            )
    }
}
