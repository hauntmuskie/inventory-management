package com.lestarieragemilang.desktop.utils;

import javafx.scene.control.Alert.AlertType;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JasperLoader extends HibernateUtil {

  private static final Logger LOGGER = Logger.getLogger(JasperLoader.class.getName());

  public void showJasperReport(Map<String, Object> parameters, URL location) {
    try {
      JasperReport jasperReport = (JasperReport) JRLoader.loadObject(location);
      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
      JasperViewer.viewReport(jasperPrint, false);
    } catch (JRException e) {
      LOGGER.log(Level.SEVERE, "Error loading Jasper report", e);
      ShowAlert.showAlert(AlertType.ERROR, null, "Error loading Jasper report");
    }
  }

  private Map<String, Object> createParameters(String... params) {
    Map<String, Object> parameters = new HashMap<>();
    for (int i = 0; i < params.length; i += 2) {
      parameters.put(params[i], params[i + 1]);
    }
    return parameters;
  }

  public void showJasperReportSupplier(URL location, String nameSupplier, String contactSupplier,
      String addressSupplier, String emailSupplier) {
    Map<String, Object> parameters = createParameters(
        "nameSupplier", nameSupplier,
        "contactSupplier", contactSupplier,
        "addressSupplier", addressSupplier,
        "emailSupplier", emailSupplier);
    showJasperReport(parameters, location);
  }

  public void showJasperReportCustomer(URL location, String nameCustomer, String contactCustomer,
      String addressCustomer, String emailCustomer) {
    Map<String, Object> parameters = createParameters(
        "nameCustomer", nameCustomer,
        "contactCustomer", contactCustomer,
        "addressCustomer", addressCustomer,
        "emailCustomer", emailCustomer);
    showJasperReport(parameters, location);
  }

  public void showJasperReportCategory(URL location, String brandCategory, String typeCategory,
      String sizeCategory, String weightCategory, String unitCategory) {
    Map<String, Object> parameters = createParameters(
        "brandCategory", brandCategory,
        "typeCategory", typeCategory,
        "sizeCategory", sizeCategory,
        "weightCategory", weightCategory,
        "unitCategory", unitCategory);
    showJasperReport(parameters, location);
  }

  public void showJasperReportStock(URL location, String brandStock, String typeStock,
      String sizeStock, String weightStock, String unitStock, String stock,
      String purchasePriceStock, String sellingPriceStock) {
    Map<String, Object> parameters = createParameters(
        "brandStock", brandStock,
        "typeStock", typeStock,
        "sizeStock", sizeStock,
        "weightStock", weightStock,
        "unitStock", unitStock,
        "stock", stock,
        "purchasePriceStock", purchasePriceStock,
        "sellingPriceStock", sellingPriceStock);
    showJasperReport(parameters, location);
  }

  public void showJasperReportSales(URL location, String invoiceNumber, String customerName,
      String stockName, Integer quantity, BigDecimal price, BigDecimal subTotal, BigDecimal priceTotal) {
    Map<String, Object> parameters = createParameters(
        "invoiceNumber", invoiceNumber,
        "customerName", customerName,
        "stockName", stockName,
        "quantity", quantity.toString(),
        "price", price.toString(),
        "subTotal", subTotal.toString(),
        "priceTotal", priceTotal.toString());
    showJasperReport(parameters, location);
  }

  public void showJasperReportReturns(URL location, String returnId, String returnType,
      String invoiceNumber, String reason) {
    Map<String, Object> parameters = createParameters(
        "returnId", returnId,
        "returnType", returnType,
        "invoiceNumber", invoiceNumber,
        "reason", reason);
    showJasperReport(parameters, location);
  }

  public void showJasperReportPurchasing(URL location, String invoiceNumber, String supplierName,
      String stockName, Integer quantity, BigDecimal price, BigDecimal subTotal, BigDecimal priceTotal) {
    Map<String, Object> parameters = createParameters(
        "invoiceNumber", invoiceNumber,
        "supplierName", supplierName,
        "stockName", stockName,
        "quantity", quantity.toString(),
        "price", price.toString(),
        "subTotal", subTotal.toString(),
        "priceTotal", priceTotal.toString());
    showJasperReport(parameters, location);
  }

  public void showJasperReportBuyList(URL location, String invoicePurchasing, Date firstDate, Date secondDate) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("invoicePurchasing", "%" + invoicePurchasing + "%");
    parameters.put("firstDate", firstDate);
    parameters.put("secondDate", secondDate);
    showJasperReport(parameters, location);
  }

  public void showJasperReportBuy(URL location, Integer buyIdValue) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("invoicePurchasing", "%" + buyIdValue.toString() + "%");
    showJasperReport(parameters, location);
  }

  public void showJasperReportSell(URL location, Integer sellIdValue) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("invoiceSales", "%" + sellIdValue.toString() + "%");
    showJasperReport(parameters, location);
  }

  public void showJasperReportSellList(URL location, String invoiceSales, Date firstDate, Date secondDate) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("invoiceSales", "%" + invoiceSales + "%");
    parameters.put("firstDate", firstDate);
    parameters.put("secondDate", secondDate);
    showJasperReport(parameters, location);
  }
}