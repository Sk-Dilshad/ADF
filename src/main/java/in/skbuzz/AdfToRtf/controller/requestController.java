package in.skbuzz.AdfToRtf.controller;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.skbuzz.AdfToRtf.service.DataFormatterService;
import in.skbuzz.AdfToRtf.service.rtfFormater;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class requestController {

    @Autowired
    private DataFormatterService dataFormatterService;

    @Autowired
    private rtfFormater rtfFormater;

    @PostMapping("/rtf")
    public ResponseEntity adfData(@RequestBody String data) throws JsonProcessingException {
       boolean isJson=dataFormatterService.isJsonData(data);
       if(isJson){
           ObjectMapper mapper = new ObjectMapper();
           JsonNode actualObj = mapper.readTree(data);
           String rtf= (String) dataFormatterService.richTextFormatter(actualObj);
           return new ResponseEntity(rtf, HttpStatus.OK);
       }
      return new ResponseEntity("Please provide json data", HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/adf")
    public ResponseEntity rtfData(@RequestBody String data)  {
            JSONObject obj=rtfFormater.RtfToAdf(data);
            return new ResponseEntity(obj,HttpStatus.OK);
    }
}
