package com.srm.machinemonitor.Handlers;

import com.srm.machinemonitor.Models.Tables.Data;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.MachinesDAO;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
public class WebSocketHandlers extends TextWebSocketHandler {

    final static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    final static List<Map> clients = new CopyOnWriteArrayList<>();

    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    DataDAO dataDAO;

    @Autowired
    MachinesDAO machinesDAO;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        /*
        Subscribing here
        JsonObject should have values like lastDataTime, machineName, sensorName
         */
        System.out.println("mes : " + message.getPayload());
        JSONObject mes = new JSONObject(message.getPayload());
        System.out.println("mes : " + mes);
        if (mes.has("machineName")){
            Map currentClient = clients.get(sessions.indexOf(session));
            System.out.println(clients);
            System.out.println(sessions);
            System.out.println(sessions.indexOf(session));
            currentClient.put("machineName", mes.get("machineName"));
            currentClient.put("sensorName", mes.get("sensorName"));
            LocalDateTime dateTime;
            if (!mes.has("startDate")){
                dateTime = LocalDateTime.parse("1999-01-01 00:00:00", formatter);
            }else{
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM/dd/yyyy, HH:mm:ss");
                dateTime = (LocalDateTime.parse((CharSequence) mes.get("startDate"), formatter2));
                System.out.println(dateTime.getMonth());
            }
            currentClient.put("lastData", dateTime);
            currentClient.put("firstRequest", true);
            System.out.println(currentClient);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map data = new HashMap();
        data.put("session", session);
        data.put("machineName", "");
        data.put("sensorName", "");

        LocalDateTime dateTime = LocalDateTime.parse("1999-01-01 00:00:00", formatter);
        data.put("lastData", dateTime);
        clients.add(data);
        sessions.add(session);
        System.out.println(clients);
        System.out.println(sessions);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println(clients);
        System.out.println(sessions);
        clients.removeIf(client -> client.get("session") == session);
        sessions.remove(session);
    }

    @Scheduled(fixedRate = 1000)
    public void streamData() throws IOException {
        /*
        Message will be sent from here
        depending on the message recieved
         */
        for (int i=0; i<clients.size(); i++){
            final Map currentClient = clients.get(i);
            final List<Data> dataToSent = dataDAO.getDataFromDateSensor((LocalDateTime) currentClient.get("lastData"),
                    (String) currentClient.get("machineName"),
                    (String) currentClient.get("sensorName"));
                final WebSocketSession session = (WebSocketSession) currentClient.get("session");
                if (dataToSent.size() > 0){
                    final Map d = new HashMap<>();
                    final List<LocalDateTime> x = new ArrayList<>();
                    final List<Double> y = new ArrayList<>();
                    final List<String> dataType = new ArrayList<>();
                    for (Data data : dataToSent){
                        x.add(data.getDate());
                        y.add(Double.valueOf(data.getValue()));
                        dataType.add(data.getData_type());
                    }
                    d.put("X", x);
                    d.put("Y", y);
                    d.put("xAxis", "DateTime");
                    d.put("yAxis", dataToSent.get(0).getData_type());
                    d.put("dataType", dataType);
                    d.put("chartType", machinesDAO.getChartType((String) currentClient.get("machineName")));
                    d.put("isNewData", currentClient.get("firstRequest"));
                    currentClient.put("firstRequest", false);

                    System.out.println(dataToSent);
                    session.sendMessage(new TextMessage(new JSONObject(d).toString()));
                    currentClient.put("lastData", dataToSent.get(dataToSent.size()-1).getDate());
                }else{
                    if (currentClient.containsKey("firstRequest")){
                        if ((boolean) currentClient.get("firstRequest")){
                            currentClient.put("firstRequest", false);
                            final Map d = new HashMap<>();
                            d.put("X", new ArrayList<>());
                            d.put("Y", new ArrayList<>());
                            d.put("dataType", new ArrayList<>());
                            d.put("chartType", machinesDAO.getChartType((String) currentClient.get("machineName")));
                            d.put("isNewData", true);
                            d.put("xAxis", "DateTime");
                            session.sendMessage(new TextMessage(new JSONObject(d).toString()));
                        }
                    }
                }
        }
    }
}
