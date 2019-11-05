package com.example.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ThanhD
 */
public class ChatworkClient {

    /**
     * PARAM FOR CHAT WOR API
     * DOCS: http://download.chatwork.com/ChatWork_API_Documentation.pdf
     */
    private static final String CW_API_URL = "https://api.chatwork.com/v2";
    private static final String CW_API_TOKEN = "b8277b78013f6d479757a6afea31616a"; //Replace your ChatWork API Token
    private static final String CW_HEADER_NAME = "X-ChatWorkToken";

    /**
     * PARAM FOR CHAT BOT API
     * DOCS: SimSimi API
     */
    private static final String CB_API_KEY = "AXN38FpcSNgiJzNwnlnhEKJzdLCHyLPoI+AeTVkZ"; //Replace your Simsimi API Key
    private static final String CB_API_URL = "http://sandbox.api.simsimi.com/request.p";
//    private static final String CB_API_URL = "http://sandbox.api.simsimi.com/request.p?key=%KEY%&lc=en&ft=0.0&text=%TEXT%";
    private static final String BOT_ID = "[To:4302388]"; //Replace XXXXX to ID ChatWork of Bot
    private static final String CB_HEADER_NAME = "X-ChatBotToken";

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
        sendMessage(roomId, "TO ALL >>> BOT STARTED!");

        StringBuilder messReply = new StringBuilder();
        int mentionRoomNum = 0;
        while (!breakFlag) {
            try {
// Check the number of messages mention BOT
                BotStatus status = mapper.readValue(
                        get(CW_API_URL.concat("/rooms/").concat(roomId), CW_HEADER_NAME, CW_API_TOKEN),
                        new TypeReference<BotStatus>() {});

                mentionRoomNum = status.mentionRoomNum;
            } catch (Exception e) {
                mentionRoomNum = 0;
            }

            if (mentionRoomNum > 0) {
                getMessages(roomId).forEach(message -> {
                    if (message.body.startsWith(BOT_ID)) {
// Get message from room of ChatWork
                        String messSend = message.body.substring(message.body.indexOf("\n") + 1);
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
                        messReply.append("\n")
                                .append("[To:")
                                .append(message.account.accountId)
                                .append("] ")
                                .append(message.account.name)
                                .append("\n")
                                .append(getMessageFromBot(messSend));

// Send message of Bot to the previous sender
                        try {
                            sendMessage(roomId, "hi");
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
    public String getMessageFromBot(String text) {
        String message = StringUtils.EMPTY;
        if (StringUtils.isBlank(text)) {
            return message;
        }

        try {
            String json = get(CB_API_URL.replace("%KEY%", CB_API_KEY)
                            .replace("%TEXT%", URLEncoder.encode(text, "UTF-8")),
                    CB_HEADER_NAME, CB_API_KEY);

            String[] results = json.split(",");

            message = results[0].split(":")[1];
        } catch (Exception e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }

        return StringUtils.remove(message, "\"");
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
        } catch (IOException e) {
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
            method.addRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            method.setParameter("body", message);
            System.out.println("messsssss"+message);
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
    private String get(String path, String headerName, String apiKey) throws IOException {


        URL url = new URL("https://wsapi.simsimi.com/190410/talk");

        String jsonInputString = "{\"utext\": \"hi\", \"lang\": \"vi\"}";

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-api-key", "AXN38FpcSNgiJzNwnlnhEKJzdLCHyLPoI+AeTVkZ");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("////////////" + response.toString());
        }

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        // Get the response body output from the server
        StringBuilder calculatedOutput = new StringBuilder();
        String rawOutputLine;
        if (in.ready()) {
            while ((rawOutputLine = ((BufferedReader) in).readLine()) != null) {
                calculatedOutput.append(rawOutputLine);
            }
            System.out.println("++++++++" + calculatedOutput.toString());
            in.close();

        }


//        GetMethod method = null;
//        try {
//            method = new GetMethod(path);
//            method.addRequestHeader(headerName, apiKey);
//
//            int statusCode = httpClient.executeMethod(method);
//
//            System.out.println(statusCode+"");
//
//            System.out.println( method.getResponseBodyAsString());
//
//            if (statusCode != HttpStatus.SC_OK) {
//                throw new Exception("Response is not valid. Check your API Key or ChatWork API status. response_code = "
//                        + statusCode + ", message =" + method.getResponseBodyAsString());
//            }
//
//            return method.getResponseBodyAsString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return StringUtils.EMPTY;
//        } finally {
//            if (method != null) {
//                method.releaseConnection();
//            }
//        }
        return "cu";
    }
}
