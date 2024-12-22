# Inventory Management System

A desktop application built with JavaFX for managing inventory, customers, suppliers, and transactions for PT Lestari Era Gemilang.

## Features

- Stock Management
- Supplier Management 
- Category Management
- Customer Management
- Transaction Processing
- Returns Processing
- Report Generation

## Tech Stack

- Java
- JavaFX
- Hibernate
- JFoenix UI Components
- JasperReports

## Project Structure

```
inventory-management/
├── src/main/
│   ├── java/
│   │   └── com/lestarieragemilang/desktop/
│   │       ├── controller/      # MVC Controllers
│   │       ├── model/          # Entity Models
│   │       ├── repository/     # Data Access Layer
│   │       ├── service/        # Business Logic
│   │       └── utils/          # Helper Classes
│   └── resources/
│       └── com/lestarieragemilang/desktop/
│           ├── ui/            # FXML Layout Files
│           ├── jasper/        # Report Templates
│           └── Assets/        # Images and Resources
```

## Key Components

### Models
- Category
- Customer
- Purchasing
- Returns
- Stock
- Supplier

### Features
- Automatic ID Generation
- Real-time Search/Filtering
- Customizable Reports
- CRUD Operations for all Entities
- Form Validation
- Interactive Data Tables

### UI Components
- Modern Material Design
- Responsive Layout
- Custom Alerts/Dialogs
- Data Tables with Sorting
- Search Fields
- Dropdown Menus

## Setup & Installation

1. Clone the repository
2. Configure your database settings in `hibernate.cfg.xml`
3. Install Maven dependencies
4. Run the application

## Development

The application follows the MVC pattern:
- Models: Entity classes with JPA annotations
- Views: FXML files for UI layout
- Controllers: Java classes handling UI logic
- Services: Business logic layer
- Repositories: Data access layer using Hibernate

## Dependencies

- JFoenix: Material Design components
- Hibernate: ORM framework
- JasperReports: Report generation
- Google Guava: Utility functions

## License

This project is proprietary software for PT Lestari Era Gemilang.
