package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Strings;
import com.google.common.net.InternetDomainName;
import com.lestarieragemilang.desktop.model.User;
import com.lestarieragemilang.desktop.service.UserService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * A JavaFX application utility for handling admin user registration.
 * This class provides a graphical interface for registering new admin users
 * with email, username, and password validation.
 */
public class AdminRegistrationUtil extends Application {
    private static final UserService userService = new UserService();
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final ThemeManager themeManager = ThemeManager.getInstance();

    private TextField emailField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button submitButton;
    private Text messageText;

    /**
     * Main entry point for the JavaFX application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes and displays the admin registration form.
     * 
     * @param primaryStage The primary stage for the application
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Registrasi Admin");
        GridPane grid = createFormLayout();
        Scene scene = new Scene(grid, 400, 500);
        themeManager.applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates and configures the main form layout using GridPane.
     * 
     * @return Configured GridPane containing the registration form
     */
    private GridPane createFormLayout() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Registrasi Admin");
        scenetitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        grid.add(scenetitle, 0, 0, 2, 1);

        createFormFields(grid);
        addRequirementsText(grid);

        messageText = new Text();
        messageText.setStyle("-fx-fill: red;");
        grid.add(messageText, 0, 5, 2, 1);

        addSubmitButton(grid);

        return grid;
    }

    /**
     * Creates and adds form input fields to the grid.
     * 
     * @param grid The GridPane to add form fields to
     */
    private void createFormFields(GridPane grid) {
        grid.add(new Label("Surel:"), 0, 1);
        emailField = new TextField();
        emailField.setPromptText("contoh@domain.com");
        grid.add(emailField, 1, 1);

        grid.add(new Label("Nama Pengguna:"), 0, 2);
        usernameField = new TextField();
        grid.add(usernameField, 1, 2);

        grid.add(new Label("Kata Sandi:"), 0, 3);
        passwordField = new PasswordField();
        passwordField.setPromptText("Minimal " + MIN_PASSWORD_LENGTH + " karakter");
        grid.add(passwordField, 1, 3);
    }

    /**
     * Adds requirement text explaining validation rules to the form.
     * 
     * @param grid The GridPane to add requirements text to
     */
    private void addRequirementsText(GridPane grid) {
        Text requirementsText = new Text(
                "Persyaratan:\n" +
                        "• Surel harus mengandung @ dan domain yang valid\n" +
                        "• Nama pengguna harus unik\n" +
                        "• Kata sandi minimal " + MIN_PASSWORD_LENGTH + " karakter");
        requirementsText.setStyle("-fx-fill: gray;");
        grid.add(requirementsText, 0, 4, 2, 1);
    }

    /**
     * Adds the submit button and its event handler to the form.
     * 
     * @param grid The GridPane to add the submit button to
     */
    private void addSubmitButton(GridPane grid) {
        submitButton = new Button("Daftar Admin");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.CENTER);
        hbBtn.getChildren().add(submitButton);
        grid.add(hbBtn, 0, 6, 2, 1);
        submitButton.setOnAction(_ -> handleRegistration());
    }

    /**
     * Handles the registration process when the submit button is clicked.
     * Validates input and initiates admin registration if validation passes.
     */
    private void handleRegistration() {
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateInput(email, username, password)) {
            registerAdmin(email, username, password);
        }
    }

    /**
     * Registers a new admin user in the system.
     * Creates a new User object, generates salt for password hashing,
     * and saves the user to the database.
     *
     * @param email    User's email address
     * @param username User's username
     * @param password User's password (will be hashed before storage)
     */
    private void registerAdmin(String email, String username, String password) {
        try {
            User user = new User();
            user.setEmail(email.toLowerCase().trim());
            user.setUsername(username.trim());
            user.setName(username.trim());

            String salt = com.google.common.io.BaseEncoding.base64()
                    .encode(UUID.randomUUID().toString().getBytes())
                    .substring(0, 32);

            user.setSalt(salt);
            user.setPasswordHash(BCrypt.hashpw(password + salt, BCrypt.gensalt(12)));

            userService.save(user);

            ShowAlert.showSuccess(
                    "Admin berhasil terdaftar!\n\n" +
                            "Surel: " + email + "\n" +
                            "Nama Pengguna: " + username + "\n\n" +
                            "Harap simpan kredensial ini dengan aman!");

            ClearFields.clearFields(emailField, usernameField, passwordField);
            messageText.setText("");
        } catch (Exception e) {
            System.err.println("Failed to register admin: " + e.getMessage());
            ShowAlert.showDatabaseError("Gagal mendaftarkan admin: " + e.getMessage());
        }
    }

    /**
     * Validates user input according to business rules.
     * Checks for:
     * - Non-empty fields
     * - Valid email format
     * - Minimum password length
     * - Username uniqueness
     * - Email uniqueness
     *
     * @param email    User's email address to validate
     * @param username User's username to validate
     * @param password User's password to validate
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInput(String email, String username, String password) {
        if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            ShowAlert.showValidationError("Semua kolom harus diisi");
            return false;
        }

        String[] emailParts = email.split("@");
        if (emailParts.length != 2 || !InternetDomainName.isValid(emailParts[1])) {
            ShowAlert.showValidationError("Format surel tidak valid");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            ShowAlert.showValidationError("Kata sandi minimal " + MIN_PASSWORD_LENGTH + " karakter");
            return false;
        }

        if (userService.isUsernameExists(username)) {
            ShowAlert.showValidationError("Nama pengguna sudah digunakan");
            return false;
        }

        if (userService.isEmailExists(email)) {
            ShowAlert.showValidationError("Surel sudah terdaftar");
            return false;
        }

        return true;
    }
}
