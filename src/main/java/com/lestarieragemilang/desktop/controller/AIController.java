package com.lestarieragemilang.desktop.controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.cdimascio.dotenv.Dotenv;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class AIController {

    @FXML
    private JFXButton aiGenerateButton;

    @FXML
    private WebView aiResponseWebView;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public AIController() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("GEMINI_API_KEY");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
    }

    @FXML
    void aiGenerateReportButton(ActionEvent event) {
        aiGenerateButton.setDisable(true);
        updateWebView("Generating report...");

        CompletableFuture.runAsync(this::generateReport, executorService)
                .exceptionally(e -> {
                    handleError("Error generating report: " + e.getMessage());
                    return null;
                });
    }

    private void generateReport() {
        String prompt = "Generate a brief report on the current state of AI technology. Use markdown formatting.";

        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        content.addProperty("text", prompt);
        JsonObject contents = new JsonObject();
        contents.add("parts", content);
        requestBody.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            String generatedText = extractGeneratedText(jsonResponse);
            String htmlContent = convertMarkdownToHtml(generatedText);
            updateWebView(htmlContent);
        } catch (IOException | InterruptedException e) {
            handleError("Error generating report: " + e.getMessage());
        }
    }

    private String extractGeneratedText(JsonObject jsonResponse) {
        try {
            return jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private String convertMarkdownToHtml(String markdown) {
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }

    private void updateWebView(String htmlContent) {
        Platform.runLater(() -> {
            aiResponseWebView.getEngine().loadContent(htmlContent);
            aiGenerateButton.setDisable(false);
        });
    }

    private void handleError(String errorMessage) {
        Platform.runLater(() -> {
            updateWebView("<p style='color: red;'>" + errorMessage + "</p>");
            aiGenerateButton.setDisable(false);
        });
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}