package com.lestarieragemilang.desktop.utils.unit;

import com.lestarieragemilang.desktop.utils.AdminRegistrationUtil;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class AdminRegistrationUtilTest {

    private AdminRegistrationUtil adminRegistrationUtil;

    @BeforeEach
    public void setUp() {
        adminRegistrationUtil = new AdminRegistrationUtil();
    }

    @Test
    @DisplayName("Validate Input All Fields Empty Returns False")
    @Description("Validation of input data when all fields are empty")
    public void testValidateInput_AllFieldsEmpty_ReturnsFalse() {
        // Arrange
        String email = "";
        String username = "";
        String password = "";

        // Act
        boolean result = invokePrivateMethod(adminRegistrationUtil, "validateInput", email, username, password);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate Input Email Exists Returns False")
    @Description("Validating the input data when the email exists")
    public void testValidateInput_EmailExists_ReturnsFalse() {
        // Arrange
        String email = "existing@email.com";
        String username = "user1";
        String password = "password";

        // Act
        boolean result = invokePrivateMethod(adminRegistrationUtil, "validateInput", email, username, password);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate Input Username Exists Returns False")
    @Description("Validating input data when username exists")
    public void testValidateInput_UsernameExists_ReturnsFalse() {
        // Arrange
        String email = "newemail@email.com";
        String username = "existinguser";
        String password = "password";

        // Act
        boolean result = invokePrivateMethod(adminRegistrationUtil, "validateInput", email, username, password);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate Input Valid Input Returns True")
    @Description("Validation of input data with correct values")
    public void testValidateInput_ValidInput_ReturnsTrue() {
        // Arrange
        String email = "newemail@email.com";
        String username = "newuser";
        String password = "password";

        // Act
        boolean result = invokePrivateMethod(adminRegistrationUtil, "validateInput", email, username, password);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Register Admin Exception Registration Fails")
    @Description("Checking administrator registration when an exception occurs")
    public void testRegisterAdmin_Exception_RegistrationFails() {
        // Arrange
        String email = "newemail@email.com";
        String username = "newuser";
        String password = "password";

        // Act
        try {
            invokePrivateMethod(adminRegistrationUtil, "registerAdmin", email, username, password);
            fail("Exception expected");
        } catch (Exception e) {
            // Assert
            assertTrue(e.getMessage().contains("Error registering admin"));
        }
    }

    private boolean invokePrivateMethod(Object instance, String methodName, String email, String username, String password) {
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, String.class, String.class, String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(instance, email, username, password);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}