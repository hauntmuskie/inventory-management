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

import com.lestarieragemilang.desktop.model.*;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import java.util.List;

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
    private final GenericService<Stock> stockService;
    private final GenericService<Sales> salesService;
    private final GenericService<Purchasing> purchasingService;
    private final GenericService<Returns> returnsService;

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
        this.stockService = new GenericService<>(new GenericDao<>(Stock.class), "STK", 1000);
        this.salesService = new GenericService<>(new GenericDao<>(Sales.class), "SLS", 1000);
        this.purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class), "PRC", 1000);
        this.returnsService = new GenericService<>(new GenericDao<>(Returns.class), "RTN", 1000);
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

    private String generateDataPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analisis data inventori berikut dan buat laporan bisnis yang komprehensif dalam bahasa Indonesia:\n\n");

        // Add Stock Data
        List<Stock> stocks = stockService.findAll();
        prompt.append("Data Stok Saat Ini:\n");
        for (Stock stock : stocks) {
            prompt.append(String.format("- %s (ID: %s): Jumlah: %d, Harga Beli: Rp%.2f, Harga Jual: Rp%.2f\n",
                    stock.getCategory().getBrand(),
                    stock.getStockId(),
                    stock.getQuantity(),
                    stock.getPurchasePrice(),
                    stock.getSellingPrice()));
        }

        // Add Sales Data
        List<Sales> sales = salesService.findAll();
        prompt.append("\nData Penjualan Terakhir:\n");
        for (Sales sale : sales) {
            prompt.append(String.format("- Faktur: %s, Tanggal: %s, Jumlah: %d, Total: Rp%.2f\n",
                    sale.getInvoiceNumber(),
                    sale.getSaleDate(),
                    sale.getQuantity(),
                    sale.getPriceTotal()));
        }

        // Add Purchase Data
        List<Purchasing> purchases = purchasingService.findAll();
        prompt.append("\nData Pembelian Terakhir:\n");
        for (Purchasing purchase : purchases) {
            prompt.append(String.format("- Faktur: %s, Tanggal: %s, Jumlah: %d, Total: Rp%.2f\n",
                    purchase.getInvoiceNumber(),
                    purchase.getPurchaseDate(),
                    purchase.getQuantity(),
                    purchase.getPriceTotal()));
        }

        // Add Returns Data
        List<Returns> returns = returnsService.findAll();
        prompt.append("\nData Retur Terakhir:\n");
        for (Returns returnItem : returns) {
            prompt.append(String.format("- ID Retur: %s, Tanggal: %s, Tipe: %s\n",
                    returnItem.getReturnId(),
                    returnItem.getReturnDate(),
                    returnItem.getReturnType()));
        }

        prompt.append("\nBerikan analisis dalam bentuk narasi untuk:\n");
        prompt.append("1. Ringkasan kondisi stok dan nilai inventori saat ini\n");
        prompt.append("2. Identifikasi potensi kehabisan stok atau kelebihan stok\n");
        prompt.append("3. Analisis performa penjualan dan tren\n");
        prompt.append("4. Pola pembelian dan hubungan dengan supplier\n");
        prompt.append("5. Analisis tingkat retur dan rekomendasi kontrol kualitas\n");
        prompt.append("6. Metrik kinerja utama dan rekomendasi\n\n");
        prompt.append("Berikan laporan dalam format naratif dengan poin-poin penting. Hindari penggunaan tabel. ");
        prompt.append("Gunakan bahasa yang formal dan profesional. ");
        prompt.append("Format menggunakan markdown dengan penekanan pada struktur narasi yang jelas.\n\n");
        prompt.append("Cantumkan juga rekomendasi konkret untuk peningkatan bisnis.");

        return prompt.toString();
    }

    private void generateReport() {
        if (cachedReport != null) {
            updateWebView(cachedReport);
            return;
        }

        String prompt = generateDataPrompt();

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
                    + "<style>"
                    + "body { font-family: 'Inter', sans-serif; padding: 20px; line-height: 1.6; }"
                    + "h1, h2, h3 { color: #2c3e50; margin-top: 1.5em; }"
                    + "p { margin-bottom: 1em; text-align: justify; }"
                    + "ul, ol { margin-bottom: 1em; }"
                    + "li { margin-bottom: 0.5em; }"
                    + ".highlight { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 1em 0; }"
                    + "</style></head><body>"
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