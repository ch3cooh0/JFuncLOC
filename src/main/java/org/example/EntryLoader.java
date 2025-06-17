package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EntryLoader {
    public static List<ExternalEntry> load(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonFile, new TypeReference<List<ExternalEntry>>() {});
    }
}
