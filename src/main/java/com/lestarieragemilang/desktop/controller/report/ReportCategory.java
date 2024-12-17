package com.lestarieragemilang.desktop.controller.report;

import com.lestarieragemilang.desktop.model.Category;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.net.URL;
import java.util.List;

public class ReportCategory {

  @FXML
  private TableView<Category> categoryTable;

  @FXML
  private TextField categorySearchField;

  private FilteredList<Category> filteredData;

  @FXML
  void printJasperCategory(MouseEvent event) {
    String path = "/jasper/category-list.jasper";
    URL url = getClass().getResource(path);
    if (url == null) {
      path = "/com/lestarieragemilang/desktop/jasper/category-list.jasper";
      url = getClass().getResource(path);
    }

    if (url == null) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("Could not find report template");
      alert.showAndWait();
      return;
    }

    Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
    if (selectedCategory == null) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Information");
      alert.setHeaderText(null);
      alert.setContentText("Please select a category to print");
      alert.showAndWait();
      return;
    }

    try {
      JasperLoader loader = new JasperLoader();
      loader.showJasperReportCategory(url,
          selectedCategory.getBrand(),
          selectedCategory.getProductType(),
          selectedCategory.getSize(),
          selectedCategory.getWeight().toString(),
          selectedCategory.getWeightUnit(),
          event
      );
    } catch (Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("Error generating report: " + e.getMessage());
      alert.showAndWait();
    }
  }

  @FXML
  void categorySearch() {
    String searchText = categorySearchField.getText().toLowerCase();
    filteredData.setPredicate(category -> {
      if (searchText == null || searchText.isEmpty()) {
        return true;
      }
      return category.getCategoryId().toLowerCase().contains(searchText) ||
          category.getBrand().toLowerCase().contains(searchText) ||
          category.getProductType().toLowerCase().contains(searchText) ||
          category.getSize().toLowerCase().contains(searchText) ||
          (category.getWeightUnit() != null && 
           category.getWeightUnit().toLowerCase().contains(searchText));
    });
  }

  @FXML
  void initialize() {
    List<Category> categories = fetchCategoriesFromDatabase();
    setupTable(categories);
    setupSearch();
  }

  private List<Category> fetchCategoriesFromDatabase() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      Query<Category> query = session.createQuery("FROM Category", Category.class);
      return query.list();
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.emptyObservableList();
    }
  }

  private void setupTable(List<Category> categories) {
    List<TableColumn<Category, ?>> columns = List.of(
        TableUtils.createColumn("Category ID", "categoryId"),
        TableUtils.createColumn("Brand", "brand"),
        TableUtils.createColumn("Product Type", "productType"),
        TableUtils.createColumn("Size", "size"),
        TableUtils.createColumn("Weight", "weight"),
        TableUtils.createColumn("Weight Unit", "weightUnit")
    );

    TableUtils.populateTable(categoryTable, columns, categories);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(categoryTable.getItems(), p -> true);
    categoryTable.setItems(filteredData);
    categorySearchField.textProperty().addListener((observable, oldValue, newValue) -> categorySearch());
  }
}