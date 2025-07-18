package com.theitdojo.optimizing_llm_responses_with_rag_in_java.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import org.springframework.ai.document.Document;
import java.util.List;

@Component
public class PDFIngestionStrategy implements IngestionStrategy {
    private static final Logger log = LoggerFactory.getLogger(PDFIngestionStrategy.class);
    private static final String STRATEGY_NAME = "pdf";

    @Override
    public List<Document> ingest(Resource resource) {
        log.info("Ingestando PDF: {}", resource.getFilename());
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        List<Document> docs = reader.get();           // 1 Document por página
        log.info("→ {} páginas convertidas a Document", docs.size());
        return docs;
    }

    @Override public String getStrategyName() { return STRATEGY_NAME; }
}