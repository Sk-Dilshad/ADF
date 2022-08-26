package in.skbuzz.AdfToRtf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

@Service
public class rtfFormaterImpl implements rtfFormater {


    JSONObject adf=new JSONObject();
    JSONArray content=new JSONArray();
    JSONArray  HelperArray=new JSONArray();
    JSONObject HelperObject=new JSONObject();

    ArrayList<String> tags=new ArrayList<>();



//    Stack s=new Stack();

    //to store data
    JSONArray ar=new JSONArray();
    JSONObject subObject=new JSONObject();


    @Override
    public JSONObject RtfToAdf(String data) {

//        try {
//            Field changeMap = adf.getClass().getDeclaredField("map");
//            changeMap.setAccessible(true);
//            changeMap.set(adf, new LinkedHashMap<>());
//            changeMap.setAccessible(false);
//        } catch (IllegalAccessException | NoSuchFieldException e) {
////            log.info(e.getMessage());
//            System.out.println("Message"+e.getMessage());
//        }

        adf.put("version","1");
        adf.put("type","doc");
        Document document= Jsoup.parse(data);
        Elements elements = document.body().children();

        for(Element e:elements){
            if(e.tagName().equals("table")){
                JSONArray RowData=new JSONArray();
                Elements rows = e.select("tr");
                JSONArray tdArray=new JSONArray();
                for(int i=0;i<rows.size();i++){
                    Elements rowData=  rows.get(i).select("td");
                    for(int j=0;j<rowData.size();j++){
                        HelperObject =new JSONObject();
                        JSONObject object=new JSONObject();
                        JSONObject tdData=  setContent(rowData.get(j));
                        JSONArray arr=new JSONArray();
                        arr.add(tdData);
                        for(int k=0;k<tags.size();k++){
                            if(!getTag(tags.get((tags.size()-1)-k)).isEmpty()){
                                object =new JSONObject();
                                object.put("type",getTag(tags.get((tags.size()-1)-k)));
                                object.put("content",arr);
                                arr=new JSONArray();
                                arr.add(object);
                          }
                      }
                        //add the tds
                        tdArray.add(arr.get(0));
                        tags =new ArrayList<>();
                    }
                    //add tds into rows
                    JSONObject tr=new JSONObject();
                    tr.put("type",getTag(rows.get(i).tagName()));
                    tr.put("content",tdArray);
                    RowData.add(tr);
                    tdArray =new JSONArray();

                }
                //Add rows into table.
                JSONObject AddTableData=new JSONObject();
                AddTableData.put("type",getTag(e.tagName()));
                AddTableData.put("content",RowData);
                content.add(AddTableData);
            }
//            else if(e.tagName().equals("ol") || e.tagName().equals("ul")){

            else {
                if(e.childrenSize()>0){
                    JSONArray main=new JSONArray();
                    for(int i=0;i<e.childrenSize();i++){
                        HelperObject =new JSONObject();
                        JSONObject object=new JSONObject();
                        JSONObject tagData=  setContent(e.child(i));
                        ArrayList marks=seprationOfMarks(tags);
                        ArrayList marksList=new ArrayList();
                        if(!marks.isEmpty()){
                            for(int j=0;j<marks.size();j++){
                                if(!getMarks((String) marks.get(j)).isEmpty()){
                                    JSONObject m=new JSONObject();
                                    m.put("type",getMarks((String) marks.get((marks.size()-1)-j)));
                                    marksList.add(m);
                                }

                            }
                        }
                        if(!marksList.isEmpty())
                            tagData.put("marks",marksList);
                        JSONArray arr=new JSONArray();
                        arr.add(tagData);

                        for(int k=0;k<tags.size();k++){
                            if(!getTag(tags.get((tags.size()-1)-k)).isEmpty()){
                                object =new JSONObject();
                                if(getTag(tags.get((tags.size()-1)-k)).equals("heading")){
                                    String level= String.valueOf(((String) tags.get((tags.size() - 1) - k)).charAt(1));
                                    JSONObject attrs=new JSONObject();
                                    attrs.put("level",level);
                                    object.put("attrs",attrs);
                                }
                                object.put("type",getTag( tags.get((tags.size()-1)-k)));
                                object.put("content",arr);
                                arr=new JSONArray();
                                arr.add(object);
                            }
                        }
                        main.add(arr.get(0));
                        tags =new ArrayList<>();

                    }
                    JSONObject finalContent=new JSONObject();
                    finalContent.put("type",getTag(e.tagName()));
                    finalContent.put("content",main);
                    content.add(finalContent);
                }
                else{
                    JSONObject singleTag =new JSONObject();
                    JSONArray singleTagArray= new JSONArray();
                    singleTag.put("type","text");
                    singleTag.put("text",e.ownText());
                    singleTagArray.add(singleTag);
                    singleTag =new JSONObject();
                    //TO-DO if has attributes to single tag.
                    System.out.println(e.attributes());
                    if(getTag(e.tagName()).equals("heading")){
                        String level= String.valueOf(e.tagName().charAt(1));
                        JSONObject attrs=new JSONObject();
                        attrs.put("level",level);
                        singleTag.put("attrs",attrs);
                    }
                    singleTag.put("type",getTag(e.tagName()));
                    singleTag.put("content",singleTagArray);
                    content.add(singleTag);


                }

            }

        }

        adf.put("content",content);
        return adf;
    }


    private JSONObject setContent(Element element){
        if(element.childrenSize()>=1){
           for(int i=0;i<element.childrenSize();i++){
               tags.add(element.tagName());
               setContent(element.child(i));
           }
        }
        else{
            HelperObject.put("type","text");
            HelperObject.put("text",element.ownText());
            tags.add(element.tagName());
        }
        return HelperObject;
    }
//    private JSONArray setContent(Element element,int count){
//
//        if(element.childrenSize()>=1){
//            count++;
//            for(int i=0;i<element.childrenSize();i++){
//                tags.add(getTag(element.tagName()));
//                setContent(element.child(i),count);
////                tags.add(getTag(element.tagName()));
//            }
//        }
//        else {
//
//            subObject =new JSONObject();
//            ar = new JSONArray();
//            subObject.put("type","text");
//            subObject.put("text",element.ownText());
//            ar.add(subObject);
//            subObject =new JSONObject();
//            subObject.put("type",getTag(element.tagName()));
//            if(getTag(element.tagName()).equals("heading")){
//                System.out.println( "charecter at"+element.tagName().charAt(1));
//                JSONObject attr=new JSONObject();
//                attr.put("level",element.tagName().charAt(1));
//                subObject.put("attrs",attr);
//            }
//            subObject.put("content",ar);
//            HelperArray.add(subObject);
//
//        }
//     return  HelperArray;
//    }
    private String getTag(String  tag){
        if(tag.equals("p"))
            return "paragraph";
        if(tag.equals("li"))
            return "listItem";
        if(tag.equals("ul"))
            return "UnOrderList";
        if(tag.equals("ol"))
            return "orderedList";
        if(tag.equals("h1") || tag.equals("h2") || tag.equals("h3")||tag.equals("h4")||tag.equals("h5")||tag.equals("h6"))
            return "heading";
        if(tag.equals("tr"))
            return "tableRow";
        if(tag.equals("td"))
            return "tableCell";
        if(tag.equals("table"))
            return "table";
        if(tag.equals("tableHeader"))
            return "th";
        return "";
    }

    private String getMarks(String tag){
        if(tag.equalsIgnoreCase("u"))
            return "underline";
        if(tag.equalsIgnoreCase("strong"))
            return "strong";
        if(tag.equalsIgnoreCase("i"))
            return "italic";
        if(tag.equalsIgnoreCase("mark"))
            return "underline";
        if(tag.equalsIgnoreCase("em"))
            return "em";
        return "";
    }
    private ArrayList seprationOfMarks(ArrayList marks){
        ArrayList marksList=new ArrayList();
        for(int i=0;i<marks.size();i++){
            if(!getMarks((String) marks.get(i)).isEmpty()){
                marksList.add(marks.get(i));
            }
        }
        return marksList;
}
    private ArrayList seprationOfTags(ArrayList tags){
        ArrayList tagList=new ArrayList();
        for(int i=0;i<tags.size();i++){
            if(!getTag((String) tags.get(i)).isEmpty()){
                tagList.add(tags.get(i));
            }
        }
        return tagList;
    }


}
