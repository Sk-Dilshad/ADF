package in.skbuzz.AdfToRtf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class DataFormateServiceImpl implements DataFormatterService {
    ArrayList<String> tags=new ArrayList<>();
    ArrayList<String> subTag=new ArrayList<>();

    String rtf="";
    @Override
    public boolean isJsonData(String data) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String richTextFormatter(JsonNode adf) {
        if(adf.isObject()) {
            Iterator<String> fieldNames = adf.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = adf.get(fieldName);
                if(fieldName.equals("content")) {
                    for(JsonNode node :fieldValue){
                        String type=node.at("/type").asText();
                        String attr=node.at("/attrs").at("/level").asText();
                        addTag(type,attr);
                        if(!type.equals("text")){
                            JsonNode content=node.at("/content");
                                   for(JsonNode contentNode:content){
                                       getContent(contentNode);
                                       for(int i=0;i<subTag.size();i++){
                                           rtf = rtf +"</" + subTag.get(i) + ">"+"\n";
                                       }
                                       subTag.clear();
//                                       closeTag();
                                   }
                            closeTag();
                         }
                         else{
                             String text=node.at("/text").asText();
                            JsonNode marks=node.at("/marks");
                            addText(text,marks);
//                            rtf +=text;
                            closeTag();
                         }
                    }
                }
                else{
                    String tag=fieldValue.asText();
                    if(!getTag(tag).isEmpty()){
                        tags.add(getTag(tag));
                        rtf=rtf+"<"+tags.get(tags.size()-1)+">"+"\n";
                    }
                }
            }
        }
        closeTag();
        return rtf;
    }
private void getContent(JsonNode content){
    for(JsonNode node:content){
//        System.out.println("node = " + node);
       if(node.isObject()){
           String type=node.at("/type").asText();
           if(type.equals("text")){
               String text=node.at("/text").asText();
               JsonNode marks=node.at("/marks");
               addText(text,marks);
//               rtf +=text;

           }
           else if(type.equals("hardBreak")){
//               String text=node.at("/text").asText();
               rtf+="\n";
           }
           else{
               String attr=node.at("/attrs").at("/level").asText();
               addTag(type,attr);
               JsonNode subnode=node.at("/content");
               if(!subnode.isEmpty()){
                   getContent(subnode);

               }
           }
       }
       else if(node.isArray()){
           for(JsonNode jnode:node){
               String type=jnode.at("/type").asText();
               if(jnode.isArray()||jnode.isObject()){
                   if(type.equals("text")){
                       String text=jnode.at("/text").asText();
                       JsonNode marks=jnode.at("/marks");
                       addText(text,marks);
//                       rtf +=text;

                   }
                   else if(type.equals("hardBreak")){
//                       String text=jnode.at("/text").asText();
                       rtf+="\n";
                   }
                   else{
                       String attr=jnode.at("/attrs").at("/level").asText();
                       addTag(type,attr);

                       JsonNode subnode=jnode.at("/content");
                       if(!subnode.isEmpty()){
                           getContent(subnode);
                       }
                   }
               }
               else{
//                   System.out.println("type = " + type);
                   String tag=node.asText();
                   if(!getTag(tag).isEmpty()) {
                       subTag.add(getTag(tag));
                       rtf = rtf + "<" + subTag.get(subTag.size() - 1) + ">" + "\n";
                   }
               }
           }
       }
       else{
           String tag=node.asText();
           if(!getTag(tag).isEmpty()) {
//               addTagInSubTag(getTag(tag),attrs);
               subTag.add(getTag(tag));
               rtf = rtf + "<" + subTag.get(subTag.size() - 1) + ">" + "\n";
           }
           else{
               if(tag.equals("hardBreak"))
                   rtf+="\n";
               else if(!tag.equals("text")){
                   JsonNode marks=content.at("/marks");
                   addText(tag,marks);
//                   rtf+=tag;
               }
           }
       }
        for(int i=2;i<tags.size();i++){
            closeTag();
        }
    }
    return ;
}
    private void  addTag(String type , String attr){
    if(!attr.isEmpty()){
        if(!getTag(type).isEmpty()) {
            tags.add(getTag(type).concat(attr));
            rtf = rtf +"<" + tags.get(tags.size() - 1) + ">"+"\n";
        }
    }
    else{
        if(!getTag(type).isEmpty()) {
            tags.add(getTag(type).concat(attr));
            rtf = rtf +"<" + tags.get(tags.size() - 1) + ">"+"\n";
        }
    }

}

//    private void  addTagInSubTag(String type , String attr){
//        if(!attr.isEmpty()){
//            if(!getTag(type).isEmpty()) {
//                subTag.add(getTag(type).concat(attr));
//                rtf = rtf +"<" + subTag.get(subTag.size() - 1) + ">"+"\n";
//            }
//        }
//        else{
//            if(!getTag(type).isEmpty()) {
//                subTag.add(getTag(type).concat(attr));
//                rtf = rtf +"<" + subTag.get(subTag.size() - 1) + ">"+"\n";
//            }
//        }
//    }
    private void addText(String text ,JsonNode marks){
    ArrayList ml=new ArrayList();
    if(!marks.isEmpty()){
        for (JsonNode mark:marks){
            String m=mark.at("/type").asText();
            if(!getMarks(m).isEmpty()){
                ml.add(getMarks(m));
                rtf+="<"+ml.get(ml.size()-1)+">";
            }
        }
        rtf +=text;
        for(int i=ml.size()-1;i>=0;i--){
            rtf+="</"+ml.get(i)+">";
        }
    }
    else{
        rtf +=text;
    }
}

    private String getTag(String tag){
        if(tag.equals("paragraph"))
            return "p";
        if(tag.equals("heading"))
            return "h";
        if(tag.equals("orderedList"))
            return "ol";
        if(tag.equals("listItem"))
            return "li";
        if(tag.equals("unOrderedList"))
            return "ul";
        if(tag.equals("doc"))
            return "document";
        if(tag.equals("table"))
            return "table";
        if(tag.equals("tableRow"))
            return "tr";
        if(tag.equals("tableCell"))
            return "td";
        if(tag.equals("tableHeader"))
            return "th";
        return "";

    }

    private String getMarks(String tag){
        if(tag.equals("underline"))
            return "u";
        if(tag.equals("em"))
            return "em";
        if(tag.equals("strong"))
            return "strong";
        return "";
    }
    private void closeTag(){
    if(!tags.isEmpty()){
        rtf=rtf+"</"+tags.get(tags.size()-1)+">"+"\n";
        tags.remove(tags.size()-1);
    }
}
}