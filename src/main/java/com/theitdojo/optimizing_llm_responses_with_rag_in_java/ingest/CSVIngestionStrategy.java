package com.theitdojo.optimizing_llm_responses_with_rag_in_java.ingest;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CSVIngestionStrategy implements IngestionStrategy {
    private static final Logger log = LoggerFactory.getLogger(CSVIngestionStrategy.class);
    private static final String STRATEGY_NAME = "csv";

    @Override
    public List<Document> ingest(Resource resource) throws Exception {
        log.info("Ingestando CSV: {}", resource.getFilename());
        List<Document> docs = new ArrayList<>();

        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVReader csv = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

            String[] line;
            while ((line = csv.readNext()) != null) {
                // Campo 0 → "RESOLUCIÓN FECHA"
                String[] parts = line[0].split("\\s*\\n\\s*", 2);
                String resolucion = parts[0].trim();
                String fecha      = parts.length > 1 ? parts[1].trim() : "";

                String content = """
RESOLUCIÓN: %s
FECHA: %s
ENTIDAD: %s
INCUMPLIMIENTO: %s
TIPO DE SANCIÓN: %s
""".formatted(resolucion, fecha, line[1], line[2], line[3]);

                docs.add(Document.builder()
                        .text(content)
                        .metadata("resolucion", resolucion)
                        .metadata("fecha", fecha)
                        .metadata("entidad", line[1])
                        .metadata("source", resource.getFilename())
                        .build());
            }
        }
        log.info("→ {} documentos creados desde {}", docs.size(), resource.getFilename());
        return docs;
    }

    @Override public String getStrategyName() { return STRATEGY_NAME; }
}