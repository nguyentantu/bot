package com.example.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by ThanhD
 */
public class ChatworkClient {

    /**
     * PARAM FOR CHAT WOR API
     * DOCS: http://download.chatwork.com/ChatWork_API_Documentation.pdf
     */
    private static final String CW_API_URL = "https://api.chatwork.com/v2";
    private static final String CW_API_TOKEN = "b8277b78013f6d479757a6afea31616a"; //ChatWork API Token
    private static final String CW_HEADER_NAME = "X-ChatWorkToken";

    /**
     * PARAM FOR CHAT BOT API
     * DOCS: SimSimi API
     */
    private static final String CB_API_KEY = "S1nr1xpuH5AyQyRGukB/IFK7gZ9VB6+LO3nuP77B"; //Simsimi API Key
    private static final String CB_API_URL = "http://sandbox.api.simsimi.com/request.p";
    private static final String BOT_ID = "[To:4302388]"; //to ID ChatWork of Bot
    private static final String CB_HEADER_NAME = "X-ChatBotToken";
    private static final String ID = "4302388";
    private static final String GREETING = "Chém gió không có gì là xấu, " +
            "quan trọng là có biết cái gì đâu mà chém ^-^ (devil)";

    private boolean breakFlag = false;

    HttpClient httpClient = new HttpClient();
    ObjectMapper mapper = new ObjectMapper();

    /**
     * BOT (API SIMSIMI)
     *
     * @param roomId
     * @throws Exception
     */
    public void startChatBot(String roomId) throws Exception {
        System.out.println("**********BOT STARTED**********");

        // Notify to room
        sendMessage(roomId, "CHÀO CÁC CẬU, LẠI LÀ MÌNH !! (devil)");
        StringBuilder messReply = new StringBuilder();
        int mentionRoomNum = 0;
        while (!breakFlag) {
            try {
        // Check the number of messages mention BOT
                BotStatus status = mapper.readValue(
                        get(CW_API_URL.concat("/rooms/").concat(roomId), CW_HEADER_NAME, CW_API_TOKEN),
                        new TypeReference<BotStatus>() {
                        });

                mentionRoomNum = status.mentionRoomNum;
            } catch (Exception e) {
                mentionRoomNum = 0;
            }

            if (mentionRoomNum > 0) {
                getMessages(roomId).forEach(message -> {
                    if (!message.account.accountId.equals(ID) || message.body.contains(BOT_ID)
                            || message.body.startsWith("[toall]")) {
                        // Get message from room of ChatWork
                        String messSend = message.body.substring(message.body.lastIndexOf("\n") + 1);

                        // Check request BOT stop?
                        if ("STOP".equals(messSend)) {
                        // Notify to room
                            try {
                                sendMessage(roomId, "TO ALL >>> BOT STOPPED!");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            breakFlag = true;
                            return;
                        }

                        // Make new message from BOT
                        try {
                            System.out.println(messSend);
                            messReply.append("[To:")
                                    .append(message.account.accountId)
                                    .append("] ")
                                    .append(message.account.name)
                                    .append("\n")
                                    .append(getMessageFromBot(messSend))
                                    .append(" (emo)");
                            System.out.println(messReply.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Send message of Bot to the previous sender
                        try {
                            sendMessage(roomId, messReply.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Clear messReply
                        messReply.setLength(0);
                    }
                });
            }

            Thread.sleep(1500);
        }
        System.out.println("**********BOT STOPPED**********");
    }

    /**
     * Get message from BOT (Call API SIMSIMI)
     *
     * @param text
     * @return message
     */
    public String getMessageFromBot(String text) throws IOException, JSONException {
        String message;

        URL url = new URL("https://wsapi.simsimi.com/190410/talk");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestProperty("x-api-key", CB_API_KEY);
        conn.setDoOutput(true);

        String jsonInput1 = "{\"utext\": \"";
        String jsonInput2 = "\", \"lang\": \"vi\", \"text_bad_prob_max\" : "+"1.0}";
        StringBuilder inputData = new StringBuilder();
        inputData.append(jsonInput1).append(text).append(jsonInput2);
        System.out.println(inputData);

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = inputData.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JSONObject jsonObject = new JSONObject(response.toString());
            message = jsonObject.getString("atext");
            System.out.println(message);
        }
        return message;
    }

    /**
     * Get message from room.
     *
     * @param roomId
     * @return the list of Message
     * @throws IOException
     */
    private List<Message> getMessages(String roomId) {
        try {
            String json = get(CW_API_URL.concat("/rooms/").concat(roomId).concat("/messages"),
                    CW_HEADER_NAME, CW_API_TOKEN);

            if (StringUtils.isEmpty(json)) {
                return Collections.EMPTY_LIST;
            }

            return mapper.readValue(json, new TypeReference<List<Message>>() {});
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Send message to room.
     *
     * @param roomId
     * @param message
     * @throws IOException
     */
    private void sendMessage(String roomId, String message) throws IOException {
        PostMethod method = null;
        try {
            method = new PostMethod(CW_API_URL.concat("/rooms/").concat(roomId).concat("/messages"));
            method.addRequestHeader("X-ChatWorkToken", CW_API_TOKEN);
            method.addRequestHeader("Content-type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            method.setParameter("body", message);
            int statusCode = httpClient.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Response is not valid. Check your API Key or ChatWork API status. response_code = "
                        + statusCode + ", message =" + method.getResponseBodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

    }

    /**
     * Get method of APIs
     *
     * @param path
     * @return json result
     * @throws IOException
     */
    private String get(String path, String headerName, String apiKey) throws IOException, JSONException {

        GetMethod method = null;
        try {
            method = new GetMethod(path);
            method.addRequestHeader(headerName, apiKey);

            int statusCode = httpClient.executeMethod(method);

            System.out.println(statusCode+"");

            System.out.println( method.getResponseBodyAsString());

            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Response is not valid. Check your API Key or ChatWork API status. response_code = "
                        + statusCode + ", message =" + method.getResponseBodyAsString());
            }

            return method.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    public List<String> listRoom() {

        List<String> idRooms = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        HttpClient httpClient = new HttpClient();
        GetMethod method = null;
        try {
            method = new GetMethod("https://api.chatwork.com/v2/rooms");
            method.addRequestHeader("X-ChatWorkToken", "b8277b78013f6d479757a6afea31616a");

            int statusCode = httpClient.executeMethod(method);

            System.out.println(statusCode+"");

            List<Room> list= mapper.readValue(method.getResponseBodyAsString(), new TypeReference<List<Room>>() {});
            System.out.println("lissr"+list);

            for (Room room: list) {
                idRooms.add(room.room_id);
            }

//            Set<String> strings = Collections.singleton(method.getResponseBodyAsString());
//            String[] arrayOfString = convert(strings);
//            System.out.println("Array of String: " + Arrays.toString(arrayOfString));
//
//            System.out.println( "222222222222222"+method.getResponseBodyAsString());

            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Response is not valid. Check your API Key or ChatWork API status. response_code = "
                        + statusCode + ", message =" + method.getResponseBodyAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return idRooms;
    }

    public static String[] convert(Set<String> setOfString) {
        // Create String[] of size of setOfString
        String[] arrayOfString = new String[setOfString.size()];

        // Copy elements from set to string array
        // using advanced for loop
        int index = 0;
        for (String str : setOfString)
            arrayOfString[index++] = str;

        // return the formed String[]
        return arrayOfString;
    }
    }
