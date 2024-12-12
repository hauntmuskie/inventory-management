package com.lestarieragemilang.desktop.controller;

import com.lestarieragemilang.desktop.model.Category;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.net.URISyntaxException;
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
  void printJasperCategory(MouseEvent event) throws IOException, URISyntaxException {
    String path = "/com/lestarieragemilang/app/desktop/jasper/category-list.jasper";
    URL url = getClass().getResource(path).toURI().toURL();
    try {
      JasperLoader loader = new JasperLoader();
      String searchText = categorySearchField.getText();
      loader.showJasperReportCategory(url, searchText, searchText, searchText, searchText, searchText);
    } catch (Exception e) {
      e.printStackTrace();
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