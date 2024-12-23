package com.lestarieragemilang.desktop.utils;

import com.google.common.collect.Maps;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class JasperLoader {
    private static final Logger logger = LoggerFactory.getLogger(JasperLoader.class);

    static {
        DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
        context.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        context.setProperty("net.sf.jasperreports.default.font.name", "Sans Serif");
    }

    private Connection getConnection() {
        if (!HibernateUtil.isDatabaseAvailable()) {
            ShowAlert.showDatabaseError("Database tidak tersedia. Silakan periksa koneksi database.");
            throw new RuntimeException("Database tidak tersedia");
        }

        final Connection[] conn = new Connection[1];
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(connection -> conn[0] = connection);
            return conn[0];
        } catch (Exception e) {
            logger.error("Gagal membuat koneksi database", e);
            ShowAlert.showDatabaseError("Gagal membuat koneksi database: " + e.getMessage());
            throw new RuntimeException("Gagal membuat koneksi database", e);
        }
    }

    private void showReport(URL location, Map<String, Object> parameters) {
        try {
            if (location == null) {
                ShowAlert.showError("Template laporan tidak ditemukan");
                return;
            }

            if (parameters == null) {
                parameters = Maps.newHashMap();
            }
            
            parameters.put("REPORT_IGNORE_MISSING_FONT", Boolean.TRUE);
            parameters.put("REPORT_DEFAULT_FONT", "Sans Serif");

            if (!HibernateUtil.isDatabaseAvailable()) {
                ShowAlert.showDatabaseError("Database tidak tersedia");
                return;
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(location);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, getConnection());

            if (jasperPrint.getPages().isEmpty()) {
                ShowAlert.showInfo("Tidak ada data untuk ditampilkan dalam laporan");
                return;
            }

            final JasperViewer viewer = new JasperViewer(jasperPrint, false);
            Platform.runLater(() -> {
                // Set specific zoom ratio (77.52%)
                viewer.setZoomRatio(0.7752f);
                viewer.setVisible(true);
                ShowAlert.showSuccess("Laporan berhasil dibuat");
            });

        } catch (JRException e) {
            logger.error("Error saat memuat Jasper Report", e);
            ShowAlert.showError("Gagal memuat laporan: " + e.getMessage() +
                    "\nSilakan periksa template laporan");
        } catch (Exception e) {
            logger.error("Error tidak terduga", e);
            ShowAlert.showError("Terjadi kesalahan: " + e.getMessage());
        }
    }

    public void showJasperReportSupplier(URL location, String supplierId, String nameSupplier,
            String contactSupplier, String addressSupplier, String emailSupplier, MouseEvent event) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("supplierId", "%" + supplierId + "%");
        parameters.put("nameSupplier", "%" + nameSupplier + "%");
        parameters.put("contactSupplier", "%" + contactSupplier + "%");
        parameters.put("addressSupplier", "%" + addressSupplier + "%");
        parameters.put("emailSupplier", "%" + emailSupplier + "%");
        showReport(location, parameters);
    }

    public void showJasperReportCustomer(URL location, String nameCustomer, String contactCustomer,
            String addressCustomer, String emailCustomer, MouseEvent event) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("nameCustomer", "%" + nameCustomer + "%");
        parameters.put("contactCustomer", "%" + contactCustomer + "%");
        parameters.put("addressCustomer", "%" + addressCustomer + "%");
        parameters.put("emailCustomer", "%" + emailCustomer + "%");
        showReport(location, parameters);
    }

    public void showJasperReportCategory(URL location, String brandCategory, String typeCategory,
            String sizeCategory, String weightCategory, String unitCategory, MouseEvent event) {
        Map<String, Object> parameters = Maps.newHashMap();
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
        Map<String, Object> parameters = Maps.newHashMap();
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
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("invoicePurchasing", Optional.ofNullable(invoicePurchasing).orElse("%"));
        parameters.put("firstDate", Optional.ofNullable(firstDate).orElse(new Date(0)));
        parameters.put("secondDate", Optional.ofNullable(secondDate).orElse(new Date()));
        showReport(location, parameters);
    }

    public void showJasperReportBuy(URL location, Integer buyIdValue) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("invoicePurchasing", "%" + buyIdValue.toString() + "%");
        showReport(location, parameters);
    }

    public void showJasperReportSell(URL location, Integer sellIdValue) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("invoiceSales", "%" + sellIdValue.toString() + "%");
        showReport(location, parameters);
    }

    public void showJasperReportSellList(URL location, String invoiceSales, Date firstDate, Date secondDate,
            MouseEvent event) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("invoiceSales", Optional.ofNullable(invoiceSales).orElse("%"));
        parameters.put("firstDate", Optional.ofNullable(firstDate).orElse(new Date(0)));
        parameters.put("secondDate", Optional.ofNullable(secondDate).orElse(new Date()));
        showReport(location, parameters);
    }

    public void showJasperReportReturn(URL location, String returnId, String returnDate,
            String returnType, String invoiceNumber, String reason, MouseEvent event) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("idReturn", "%" + returnId + "%");
        showReport(location, parameters);
    }
}
