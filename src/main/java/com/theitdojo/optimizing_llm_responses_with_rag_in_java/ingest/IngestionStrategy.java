package com.theitdojo.optimizing_llm_responses_with_rag_in_java.ingest;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import java.util.List;

public interface IngestionStrategy {

    /**
     * Parsea el recurso y lo convierte en una lista de Document.
     */
    List<Document> ingest(Resource resource) throws Exception;

    /**
     * Nombre único de la estrategia — suele coincidir con la extensión del archivo.
     */
    String getStrategyName();
}