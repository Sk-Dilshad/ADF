package in.skbuzz.AdfToRtf.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface DataFormatterService {
    public boolean isJsonData(String data);
    public String richTextFormatter(JsonNode adf);
}
