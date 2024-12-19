package com.lestarieragemilang.desktop.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JasperLoader {
    private static final Logger LOGGER = Logger.getLogger(JasperLoader.class.getName());

    private Connection getConnection() {
        if (!HibernateUtil.isDatabaseAvailable()) {
            throw new RuntimeException("Database is not available");
        }

        final Connection[] conn = new Connection[1];
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            session.doWork(new Work() {
                public void execute(Connection connection) {
                    conn[0] = connection;
                }
            });
        } finally {
            session.close();
        }
        
        return conn[0];
    }

    private void showReport(URL location, Map<String, Object> parameters) {
        try {
            if (!HibernateUtil.isDatabaseAvailable()) {
                throw new RuntimeException("Database is not available");
            }

            if (location == null) {
                throw new RuntimeException("Report template not found. Please ensure reports are compiled.");
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(location);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, getConnection());

            final JasperViewer viewer = new JasperViewer(jasperPrint, false);
            Platform.runLater(() -> viewer.setVisible(true));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading Jasper Report", e);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading report: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void showJasperReportSupplier(URL location, String supplierId, String nameSupplier, 
            String contactSupplier, String addressSupplier, String emailSupplier, MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("supplierId", "%" + supplierId + "%");
        parameters.put("nameSupplier", "%" + nameSupplier + "%");
        parameters.put("contactSupplier", "%" + contactSupplier + "%");
        parameters.put("addressSupplier", "%" + addressSupplier + "%");
        parameters.put("emailSupplier", "%" + emailSupplier + "%");
        showReport(location, parameters);
    }

    public void showJasperReportCustomer(URL location, String nameCustomer, String contactCustomer,
            String addressCustomer, String emailCustomer, MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nameCustomer", "%" + nameCustomer + "%");
        parameters.put("contactCustomer", "%" + contactCustomer + "%");
        parameters.put("addressCustomer", "%" + addressCustomer + "%");
        parameters.put("emailCustomer", "%" + emailCustomer + "%");
        showReport(location, parameters);
    }

    public void showJasperReportCategory(URL location, String brandCategory, String typeCategory,
            String sizeCategory, String weightCategory, String unitCategory, MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("brandCategory", "%" + brandCategory + "%");
        parameters.put("typeCategory", "%" + typeCategory + "%");
        parameters.put("sizeCategory", "%" + sizeCategory + "%");
        parameters.put("weightCategory", "%" + weightCategory + "%");
        parameters.put("unitCategory", "%" + unitCategory + "%");
        showReport(location, parameters);
    }

    public void showJasperReportStock(URL location, String brandStock, String typeStock,
            String sizeStock, String weightStock, String unitStock, String stock, String purchasePriceStock,
            String sellingPriceStock, MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("brandStock", "%" + brandStock + "%");
        parameters.put("typeStock", "%" + typeStock + "%");
        parameters.put("sizeStock", "%" + sizeStock + "%");
        parameters.put("weightStock", "%" + weightStock + "%");
        parameters.put("unitStock", "%" + unitStock + "%");
        parameters.put("stock", "%" + stock + "%");
        parameters.put("purchasePriceStock", "%" + purchasePriceStock + "%");
        parameters.put("sellingPriceStock", "%" + sellingPriceStock + "%");
        showReport(location, parameters);
    }

    public void showJasperReportBuyList(URL location, String invoicePurchasing, Date firstDate, Date secondDate,
            MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invoicePurchasing", "%" + invoicePurchasing + "%");
        parameters.put("firstDate", firstDate);
        parameters.put("secondDate", secondDate);
        showReport(location, parameters);
    }

    public void showJasperReportBuy(URL location, Integer buyIdValue) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invoicePurchasing", "%" + buyIdValue.toString() + "%");
        showReport(location, parameters);
    }

    public void showJasperReportSell(URL location, Integer sellIdValue) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invoiceSales", "%" + sellIdValue.toString() + "%");
        showReport(location, parameters);
    }

    public void showJasperReportSellList(URL location, String invoiceSales, Date firstDate, Date secondDate,
            MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invoiceSales", "%" + invoiceSales + "%");
        parameters.put("firstDate", firstDate);
        parameters.put("secondDate", secondDate);
        showReport(location, parameters);
    }

    public void showJasperReportReturn(URL location, String returnId, LocalDate returnDate,
            String returnType, String invoiceNumber, String reason, MouseEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("returnId", "%" + returnId + "%");
        parameters.put("returnDate", java.sql.Date.valueOf(returnDate));
        parameters.put("returnType", "%" + returnType + "%");
        parameters.put("invoiceNumber", "%" + invoiceNumber + "%");
        parameters.put("reason", "%" + reason + "%");
        showReport(location, parameters);
    }
}
