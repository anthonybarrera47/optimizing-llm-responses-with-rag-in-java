package com.theitdojo.optimizing_llm_responses_with_rag_in_java.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

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
    private final String glossaryContext;
    private final PromptTemplate promptTemplate;


    public ChatAssistantService(ChatClient.Builder builder,
                                @Value("classpath:/system-prompt.md") Resource systemPrompt,
                                ChatMemory chatMemory,
                                @Value("classpath:/simv/glosario.txt") Resource glossaryResource,
                                @Value("classpath:/rag-prompt-template.st") Resource ragPromptTemplate,
                                VectorStore vectorStore) throws IOException {

        PromptTemplate qaTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .resource(ragPromptTemplate)
                .build();

        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(qaTemplate)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.7f).topK(4).build())
                .build();

        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        qaAdvisor
                )
                .build();

        this.promptTemplate = new PromptTemplate(ragPromptTemplate);
        this.glossaryContext = glossaryResource.getContentAsString(StandardCharsets.UTF_8);
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
    public Flux<String> askQuestionWithContext(String conversationId, String question) {
        // TODO: Implementar la lógica de RAG en un futuro ejercicio.
        // Cambiamos a Flux para soportar el streaming reactivo hacia la UI de Vaadin.
        Prompt prompt = this.promptTemplate.create(Map.of("context", this.glossaryContext, "question", question));

        return chatClient.prompt(prompt)
                .user(question)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
    
}
