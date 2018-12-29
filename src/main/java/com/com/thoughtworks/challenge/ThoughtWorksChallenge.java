package com.com.thoughtworks.challenge;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class ThoughtWorksChallenge {
    public static final String PASSWORD = "---password required----";
    private static RestTemplate restTemplate = new RestTemplate();
    private static int counter = 65;
    private static int[] arrayToCheck = new int[26];
    static int tw = 0;

    public static void main(String[] args) throws IOException {
        ThoughtWorksChallenge dm = new ThoughtWorksChallenge();
        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("userId", PASSWORD));

        ResponseEntity<String> exchange = restTemplate.exchange("http://http-hunt.thoughtworks-labs.net/challenge/input", HttpMethod.GET, dm.getHeaders(null), String.class);
        //Level1
        //HttpEntity<String> level1Response = getLevel1Response(dm, exchange);

        //Level 2 not written.

        //Level3
        //String s1 = getLevel3Response(exchange);

        //Level4
        String s = getLevel4Response(exchange);

        String s2 = restTemplate.postForObject("http://http-hunt.thoughtworks-labs.net/challenge/output", getHeaders(s), String.class);
        System.out.println(s2);
    }

    private static String getLevel4Response(ResponseEntity<String> exchange) throws IOException {
        ObjectMapper e = new ObjectMapper();
        e.registerModule(new JodaModule());
        e.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        e.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JsonNode jsonNode = (new ObjectMapper()).readTree(exchange.getBody());
        String toolUsage = jsonNode.findValue("tools").toString();
        final int maximumWeight = jsonNode.findValue("maximumWeight").asInt();

        List<Tools> o = e.readValue(toolUsage, e.getTypeFactory()
                .constructCollectionType(List.class, Tools.class));

        Collections.sort(o);

        List<String> collect = o.stream().filter(t -> (tw = (t.getWeight()) + tw) <= maximumWeight)
                .map(val -> val.getName())
                .sorted()
                .collect(Collectors.toList());

        ToolsToTakeSorted sortedtools = new ToolsToTakeSorted(collect);

        return e.writeValueAsString(sortedtools);
    }

    private static String getLevel3Response(ResponseEntity<String> exchange) throws IOException {
        ObjectMapper e = new ObjectMapper();
        e.registerModule(new JodaModule());
        e.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        e.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JsonNode jsonNode = (new ObjectMapper()).readTree(exchange.getBody());
        String toolUsage = jsonNode.findValue("toolUsage").toString();

        List<Tool> o = e.readValue(toolUsage, e.getTypeFactory()
                .constructCollectionType(List.class, Tool.class));

        Collections.sort(o);
        List<OutPut> collect = o.stream().map(i -> {
            try {
                OutPut tool = new OutPut();
                tool.setName(i.getName());
                tool.setTimeUsedInMinutes(i.getTimeInMinutes(i));
                return tool;
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
        }).collect(toList());

        Map<String, Long> map = new HashMap<>();

        List<List<OutPut>> collect1 = collect.stream().map(k -> {
            if (map.containsKey(k.getName())) {
                map.put(k.getName(), map.get(k.getName()) + k.getTimeUsedInMinutes());
                return map;
            }
            map.put(k.getName(), k.getTimeUsedInMinutes());
            return map;
        }).collect(toList()).stream().map(j -> j.entrySet().stream().map(k -> {
            OutPut outPut = new OutPut();
            outPut.setName(k.getKey());
            outPut.setTimeUsedInMinutes(k.getValue());
            return outPut;
        }).sorted().collect(toList())).collect(toList());


        ToolsSortedOnUsage t = new ToolsSortedOnUsage(collect1.get(0));
        String s1 = null;
        try {
            s1 = e.writeValueAsString(t);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s1;
    }

    private static HttpEntity<String> getLevel1Response(ThoughtWorksChallenge dm, ResponseEntity<String> exchange) throws IOException {
        JsonNode jsonNode = (new ObjectMapper()).readTree(exchange.getBody());

        String s = jsonNode.findValue("encryptedMessage").toString();
        int key = jsonNode.findValue("key").asInt();

        return dm.getHeaders(process(key, s));
    }

    //Method for Level 1
    private static String process(int key, String input) {

        Stream.iterate(0, e -> e + 1).limit(26).forEachOrdered(e -> {
            arrayToCheck[e] = counter;
            counter = counter + 1;
        });

        if (input == null) {
            return null;
        }

        char[] chars = input.toCharArray();
        int[] asciValues = new int[chars.length];
        Stream.iterate(0, e -> e + 1).limit(chars.length).forEachOrdered(e -> {
            asciValues[e] = chars[e];
        });

        Stream.iterate(0, e -> e + 1).limit(chars.length).forEachOrdered(e -> {
            if (asciValues[e] >= arrayToCheck[0] && asciValues[e] <= arrayToCheck[25]) {
                int newAscii = updateAscii(asciValues[e], arrayToCheck, key);
                asciValues[e] = newAscii;
            } else {
                asciValues[e] = asciValues[e];
            }
        });

        char[] result = new char[chars.length];
        Stream.iterate(0, e -> e + 1).limit(chars.length).forEachOrdered(e -> {
            result[e] = (char) asciValues[e];
        });
        Stream.of(result).forEach(System.out::println);
        return String.valueOf(result);
    }

    private static int updateAscii(int asciValue, int[] arrayToCheck, int key) {
        int counter = 1;
        while (counter <= key) {
            asciValue = asciValue - 1;
            if (asciValue < arrayToCheck[0]) {
                asciValue = arrayToCheck[25];
            }
            counter++;
        }
        return asciValue;
    }

    public static HttpEntity<String> getHeaders(String dataAsString) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> httpEntity = null;
        headers.set("userId", PASSWORD);
        if (dataAsString == null) {
            httpEntity = new HttpEntity<>("parameters", headers);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            /* //Used for level1 not for Level3 and Level4
            JSONObject json = new JSONObject();
            json.put("message", process);*/
            httpEntity = new HttpEntity<>(dataAsString, headers);
        }
        return httpEntity;
    }
}

//Level3 Classes
class Tool implements Comparable<Tool> {
    String name;
    String useStartTime;
    String useEndTime;

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUseStartTime() {
        return useStartTime;
    }

    public void setUseStartTime(String useStartTime) {
        this.useStartTime = useStartTime;
    }

    public String getUseEndTime() {
        return useEndTime;
    }

    public void setUseEndTime(String useEndTime) {
        this.useEndTime = useEndTime;
    }

    @Override
    public int compareTo(Tool o) {

        return (int) ((getTime(this.getUseStartTime()) - getTime(this.getUseEndTime()))
                - (getTime(o.getUseStartTime()) - getTime(o.getUseEndTime())));


    }

    public long getTime(String time) {
        Date date = null;
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = date.getTime();
        return millis;
    }

    public long getTimeInMinutes(Tool o) {
        return TimeUnit.MILLISECONDS.toMinutes(getTime(o.getUseEndTime()) - (getTime(o.getUseStartTime())));
    }


}

//Level3 Classes
class ToolsSortedOnUsage {
    List<OutPut> toolsSortedOnUsage;

    public List<OutPut> getToolsSortedOnUsage() {
        return toolsSortedOnUsage;
    }

    public void setToolsSortedOnUsage(List<OutPut> toolsSortedOnUsage) {
        this.toolsSortedOnUsage = toolsSortedOnUsage;
    }

    public ToolsSortedOnUsage(List<OutPut> list) {
        this.toolsSortedOnUsage = list;
    }
}

//Level3 Classes
class OutPut implements Comparable<OutPut> {
    String name;
    long timeUsedInMinutes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeUsedInMinutes() {
        return timeUsedInMinutes;
    }

    public void setTimeUsedInMinutes(long timeUsedInMinutes) {
        this.timeUsedInMinutes = timeUsedInMinutes;
    }

    @Override
    public int compareTo(OutPut o) {
        return (int) (o.timeUsedInMinutes - this.timeUsedInMinutes);
    }

}
//Level4 Classes

class ToolsToTakeSorted {
    List<String> toolsToTakeSorted;


    public List<String> getToolsToTakeSorted() {
        return toolsToTakeSorted;
    }

    public ToolsToTakeSorted(List<String> toolsToTakeSorted) {
        this.toolsToTakeSorted = toolsToTakeSorted;
    }
}

//Level4 Classes
class Tools implements Comparable<Tools> {
    String name;
    int weight;
    int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    @Override
    public int compareTo(Tools o) {
        int result = this.getWeight() - o.getWeight();
        if (result == 0) {
            result = o.getValue() - this.getValue();
        }
        return result;
    }
}
