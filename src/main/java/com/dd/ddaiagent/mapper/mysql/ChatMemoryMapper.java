package com.dd.ddaiagent.mapper.mysql;

import com.dd.ddaiagent.entity.ChatMemoryEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChatMemoryMapper {
    
    @Insert("INSERT INTO chat_memory(conversation_id, type, text) VALUES(#{conversationId}, #{type}, #{text})")
    int insert(@Param("conversationId") String conversationId, @Param("type") String type, @Param("text") String text);

    @Insert("<script>" +
            "INSERT INTO chat_memory (conversation_id, type, text) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.conversationId}, #{item.type}, #{item.text})" +
            "</foreach>" +
            "</script>")
    int batchInsert(List<ChatMemoryEntity> list);

    @Select("SELECT type, text FROM chat_memory WHERE conversation_id = #{conversationId} ORDER BY id ASC")
    List<ChatMemoryEntity> findByConversationId(String conversationId);
    
    @Select("SELECT type, text FROM chat_memory WHERE conversation_id = #{conversationId} ORDER BY id DESC LIMIT #{limit}")
    List<ChatMemoryEntity> findLastNByConversationId(String conversationId, int limit);
    
    @Delete("DELETE FROM chat_memory WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(String conversationId);
}