package com.theitdojo.optimizing_llm_responses_with_rag_in_java.services;

import java.util.stream.Stream;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

@Service
public class ChatAssistantService implements ChatAssistant {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatAssistantService(ChatClient.Builder builder,@Value("classpath:/system-prompt.md") Resource systemPrompt,ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Override
    public String getResponse(String conversationId, String message) {
        return this.chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .call()
                .content();
    }

    @Override
    public Stream<String> streamResponse(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .toStream();
    }


    @Override
    public Stream<String> askQuestionWithContext(String conversationId, String question) {
        // TODO: Implementar la lógica de RAG en un futuro ejercicio.
        return chatClient.prompt()
                .user(question)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .toStream();
    }
    
}
