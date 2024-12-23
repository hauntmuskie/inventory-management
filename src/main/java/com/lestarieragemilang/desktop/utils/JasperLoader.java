package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URL;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JasperLoader {
    private static final Logger logger = LoggerFactory.getLogger(JasperLoader.class);
    private static final float DEFAULT_ZOOM_RATIO = 0.7752f;
    
    private final LoadingCache<URL, JasperReport> reportCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build(new CacheLoader<URL, JasperReport>() {
            @Override
            @Nonnull
            public JasperReport load(@Nonnull URL url) throws JRException {
                return (JasperReport) JRLoader.loadObject(url);
            }
        });

    private static final ImmutableMap<String, Object> BASE_PARAMETERS = ImmutableMap.of(
        "REPORT_IGNORE_MISSING_FONT", Boolean.TRUE,
        "REPORT_DEFAULT_FONT", "Sans Serif"
    );

    static {
        DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
        context.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        context.setProperty("net.sf.jasperreports.default.font.name", "Sans Serif");
    }

    private Connection getConnection() {
        Preconditions.checkState(HibernateUtil.isDatabaseAvailable(), "Database tidak tersedia");

        final Connection[] conn = new Connection[1];
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(connection -> conn[0] = connection);
            return Preconditions.checkNotNull(conn[0], "Failed to create database connection");
        } catch (Exception e) {
            logger.error("Gagal membuat koneksi database", e);
            throw new RuntimeException("Gagal membuat koneksi database", e);
        }
    }

    private JasperReport getReport(URL location) {
        try {
            return reportCache.get(Preconditions.checkNotNull(location, "Report location cannot be null"));
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to load report template", e);
        }
    }

    private void showReport(URL location, Map<String, Object> parameters) {
        Preconditions.checkNotNull(location, "Report location cannot be null");
        
        try {
            Map<String, Object> finalParams = Maps.newHashMap(BASE_PARAMETERS);
            if (parameters != null) {
                finalParams.putAll(parameters);
            }

            JasperReport report = getReport(location);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, finalParams, getConnection());

            if (jasperPrint.getPages().isEmpty()) {
                ShowAlert.showInfo("Tidak ada data untuk ditampilkan dalam laporan");
                return;
            }

            Platform.runLater(() -> {
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setZoomRatio(DEFAULT_ZOOM_RATIO);
                viewer.setVisible(true);
                ShowAlert.showSuccess("Laporan berhasil dibuat");
            });
        } catch (Exception e) {
            logger.error("Error generating report", e);
            ShowAlert.showError("Gagal memuat laporan: " + e.getMessage());
        }
    }

    private String wrapLikeParam(String param) {
        return "%" + Strings.nullToEmpty(param) + "%";
    }

    public void showJasperReportSupplier(URL location, String... params) {
        Preconditions.checkNotNull(location, "Location cannot be null");
        ImmutableMap.Builder<String, Object> parameters = ImmutableMap.builder();
        String[] fields = {"supplierId", "nameSupplier", "contactSupplier", "addressSupplier", "emailSupplier"};
        
        for (int i = 0; i < Math.min(params.length, fields.length); i++) {
            parameters.put(fields[i], wrapLikeParam(params[i]));
        }
        
        showReport(location, parameters.build());
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
        showReport(location, ImmutableMap.of(
            "invoicePurchasing", Strings.isNullOrEmpty(invoicePurchasing) ? "%" : invoicePurchasing,
            "firstDate", com.google.common.base.Optional.fromNullable(firstDate).or(new Date(0)),
            "secondDate", com.google.common.base.Optional.fromNullable(secondDate).or(new Date())
        ));
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
