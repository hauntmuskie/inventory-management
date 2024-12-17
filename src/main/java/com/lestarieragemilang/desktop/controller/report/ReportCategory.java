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
import java.sql.SQLException;
import java.util.List;

public class ReportCategory {

  @FXML
  private TableColumn<Category, String> brandCategoryCol;

  @FXML
  private TableColumn<Category, Integer> categoryIDCol;

  @FXML
  private TableView<Category> categoryTable;

  @FXML
  private TableColumn<Category, String> sizeCategoryCol;

  @FXML
  private TableColumn<Category, String> typeCategoryCol;

  @FXML
  private TableColumn<Category, String> unitCategoryCol;

  @FXML
  private TableColumn<Category, String> weightCategoryCol;

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
    filteredData.setPredicate(category -> searchText.isEmpty() ||
        category.getBrand().toLowerCase().contains(searchText) ||
        category.getProductType().toLowerCase().contains(searchText) ||
        category.getSize().toLowerCase().contains(searchText) ||
        category.getWeightUnit().toLowerCase().contains(searchText));
  }

  @FXML
  void initialize() throws SQLException {
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
        TableUtils.createColumn("Brand", "categoryBrand"),
        TableUtils.createColumn("Category ID", "categoryID"),
        TableUtils.createColumn("Size", "categorySize"),
        TableUtils.createColumn("Type", "categoryType"),
        TableUtils.createColumn("Unit", "categoryUnit"),
        TableUtils.createColumn("Weight", "categoryWeight"));

    TableUtils.populateTable(categoryTable, columns, categories);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(categoryTable.getItems(), p -> true);
    categoryTable.setItems(filteredData);
    categorySearchField.textProperty().addListener((observable, oldValue, newValue) -> categorySearch());
  }
}