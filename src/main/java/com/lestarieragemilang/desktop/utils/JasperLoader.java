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
import java.io.File;
import java.net.URISyntaxException;

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

    private static String getResourcePath(String resourceName) {
        try {
            URL resource = JasperLoader.class.getResource(resourceName);
            if (resource == null) {
                logger.error("Resource not found: {}", resourceName);
                return "";
            }
            return new File(resource.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            logger.error("Error converting resource URL to path", e);
            return "";
        }
    }

    private static final ImmutableMap<String, Object> BASE_PARAMETERS = ImmutableMap.<String, Object>builder()
            .put("REPORT_IGNORE_MISSING_FONT", Boolean.TRUE)
            .put("REPORT_DEFAULT_FONT", "DejaVu Sans")
            .put("IMAGE_PATH", getResourcePath("/com/lestarieragemilang/desktop/jasper/image/6057567234160706437.png"))
            .build();

    static {
        DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
        context.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        context.setProperty("net.sf.jasperreports.default.font.name", "DejaVu Sans");
        context.setProperty("net.sf.jasperreports.default.pdf.font.name", "DejaVu Sans");
        context.setProperty("net.sf.jasperreports.default.pdf.encoding", "UTF-8");
        context.setProperty("net.sf.jasperreports.default.pdf.embedded", "true");
    }

    private void showReport(URL location, Map<String, Object> parameters) {
        Session session = null;
        Connection connection = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            connection = session.doReturningWork(conn -> conn);

            Map<String, Object> allParameters = Maps.newHashMap();
            allParameters.putAll(BASE_PARAMETERS);
            allParameters.putAll(parameters);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    getReport(location),
                    allParameters,
                    connection);

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
        } finally {
            try {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            } catch (Exception e) {
                logger.error("Error closing Hibernate session", e);
            }
        }
    }

    private JasperReport getReport(URL location) {
        try {
            return reportCache.get(Preconditions.checkNotNull(location, "Report location cannot be null"));
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to load report template", e);
        }
    }

    private String wrapLikeParam(String param) {
        return "%" + Strings.nullToEmpty(param) + "%";
    }

    public void showJasperReportSupplier(URL location, String... params) {
        Preconditions.checkNotNull(location, "Location cannot be null");
        ImmutableMap.Builder<String, Object> parameters = ImmutableMap.builder();
        String[] fields = { "supplierId", "nameSupplier", "contactSupplier", "addressSupplier", "emailSupplier" };

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
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("invoicePurchasing", Strings.isNullOrEmpty(invoicePurchasing) ? "%" : invoicePurchasing);
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
