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

    private String cachedReport;

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
    public void initialize() {
        displayInitialMessage();
    }

    private void displayInitialMessage() {
        String initialHtml = "<html><head>"
                + "<style>"
                + "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap');"
                + "body { font-family: 'Inter', sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }"
                + "</style></head><body>"
                + "<p>Click the generate button to create an AI report.</p>"
                + "</body></html>";

        Platform.runLater(() -> aiResponseWebView.getEngine().loadContent(initialHtml));
    }

    private void displayLoadingMessage() {
        String loadingHtml = "<html><head>"
                + "<style>"
                + "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap');"
                + "body { font-family: 'Inter', sans-serif; margin: 0; padding: 0; }"
                + ".loader-container { position: fixed; top: 20px; left: 50%; transform: translateX(-50%); }"
                + ".loader { border: 5px solid #f3f3f3; border-top: 5px solid #3498db; border-radius: 50%; width: 50px; height: 50px; animation: spin 1s linear infinite; }"
                + "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }"
                + "</style></head><body>"
                + "<div class='loader-container'><div class='loader'></div></div>"
                + "</body></html>";

        Platform.runLater(() -> aiResponseWebView.getEngine().loadContent(loadingHtml));
    }

    @FXML
    void aiGenerateReportButton(ActionEvent event) {
        aiGenerateButton.setDisable(true);
        displayLoadingMessage();

        CompletableFuture.runAsync(this::generateReport, executorService)
                .exceptionally(e -> {
                    handleError("Error generating report: " + e.getMessage());
                    return null;
                });
    }

    private void generateReport() {
        if (cachedReport != null) {
            updateWebView(cachedReport);
            return;
        }

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
            cachedReport = htmlContent;
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
            String fontCss = "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap' rel='stylesheet'>";
            String styledHtmlContent = "<html><head>" + fontCss
                    + "<style>body { font-family: 'Inter', sans-serif; padding: 20px; }</style></head><body>"
                    + htmlContent + "</body></html>";
            aiResponseWebView.getEngine().loadContent(styledHtmlContent);
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