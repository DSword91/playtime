package com.dsword91.playtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class HttpApiServer {
    private static final Logger LOGGER = LogManager.getLogger(HttpApiServer.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ServerSocket serverSocket;
    private static Thread serverThread;

    public static void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverThread = new Thread(() -> startHttpServer(serverSocket), "PlayTime-API-Server");
            serverThread.setDaemon(true);
            serverThread.start();
            LOGGER.info("HTTP API 服务器已启动在端口 {}", port);
        } catch (IOException e) {
            LOGGER.error("无法启动 HTTP API 服务器（端口 {}）", port, e);
        }
    }

    private static void startHttpServer(ServerSocket socket) {
        var executor = Executors.newCachedThreadPool();
        while (!socket.isClosed()) {
            try {
                Socket clientSocket = socket.accept();
                executor.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (!socket.isClosed()) LOGGER.error("接受客户端连接失败", e);
            }
        }
        executor.shutdown();
    }

    private static void handleClient(Socket clientSocket) {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = new HashMap<>();
            String header;
            while ((header = in.readLine()) != null && !header.isEmpty()) {
                String[] headerParts = header.split(":", 2);
                if (headerParts.length == 2) headers.put(headerParts[0].trim().toLowerCase(), headerParts[1].trim());
            }

            if ("GET".equals(method)) {
                if ("/playtime".equals(path) || path.startsWith("/playtime?")) {
                    handlePlaytimeRequest(out, path);
                } else if ("/health".equals(path)) {
                    sendJsonResponse(out, 200, Collections.singletonMap("status", "ok"));
                } else {
                    sendJsonResponse(out, 404, Collections.singletonMap("error", "Not Found"));
                }
            } else {
                sendJsonResponse(out, 405, Collections.singletonMap("error", "Method Not Allowed"));
            }
        } catch (IOException e) {
            LOGGER.error("处理客户端请求失败", e);
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                LOGGER.error("关闭连接失败", e);
            }
        }
    }

    private static void handlePlaytimeRequest(PrintWriter out, String path) throws IOException {
        Map<String, String> params = parseQueryParams(path);
        int top = 10;
        if (params.containsKey("top")) {
            try {
                top = Integer.parseInt(params.get("top"));
                if (top < 1 || top > 100) top = 10;
            } catch (NumberFormatException e) { top = 10; }
        }

        List<DataManager.PlayerRanking> leaderboard = DataManager.getInstance().getLeaderboard(top);
        Map<String, Object> response = new HashMap<>();
        response.put("total_players", leaderboard.size());
        List<Map<String, Object>> players = new ArrayList<>();
        for (DataManager.PlayerRanking r : leaderboard) {
            Map<String, Object> m = new HashMap<>();
            m.put("uuid", r.uuid);
            m.put("name", r.playerName);
            m.put("play_minutes", r.activeMinutes);
            players.add(m);
        }
        response.put("leaderboard", players);

        sendJsonResponse(out, 200, response);
    }

    private static Map<String, String> parseQueryParams(String path) {
        Map<String, String> params = new HashMap<>();
        int queryStart = path.indexOf('?');
        if (queryStart == -1) return params;
        String query = path.substring(queryStart + 1);
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return params;
    }

    private static void sendJsonResponse(PrintWriter out, int statusCode, Object data) {
        String json = GSON.toJson(data);
        out.println("HTTP/1.1 " + statusCode + " OK");
        out.println("Content-Type: application/json; charset=utf-8");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Content-Length: " + json.getBytes(StandardCharsets.UTF_8).length);
        out.println("Connection: close");
        out.println();
        out.println(json);
        out.flush();
    }
}
