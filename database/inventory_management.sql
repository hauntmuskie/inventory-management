-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 16, 2025 at 06:37 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `inventory_management`
--

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `category_id` varchar(255) NOT NULL,
  `brand` varchar(50) NOT NULL,
  `product_type` varchar(50) NOT NULL,
  `size` varchar(20) DEFAULT NULL,
  `weight` decimal(10,2) DEFAULT NULL,
  `weight_unit` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `category_id`, `brand`, `product_type`, `size`, `weight`, `weight_unit`) VALUES
(1, 'CAT-001', 'Krakatau Steel', 'Sheet', '4x8 ft', 120.50, 'kg'),
(2, 'CAT-002', 'Gunung Steel', 'Rod', '2 inch', 45.75, 'kg'),
(3, 'CAT-003', 'Master Steel', 'Beam', '6 inch', 250.00, 'kg'),
(4, 'CAT-004', 'Ispat Indo', 'Tube', '4 inch', 85.30, 'kg'),
(5, 'CAT-005', 'Gunawan Dianjaya Steel', 'Plate', '5x10 ft', 180.00, 'kg'),
(6, 'CAT-006', 'Steel Pipe Industry', 'Pipe', '8 inch', 95.40, 'kg'),
(7, 'CAT-007', 'Jayapari Steel', 'Coil', '4x8 ft', 320.00, 'kg'),
(8, 'CAT-008', 'Jaya Steel', 'Wire', '1 inch', 25.50, 'kg'),
(9, 'CAT-009', 'Indonusa Steel', 'Angle', '3 inch', 42.75, 'kg'),
(10, 'CAT-010', 'Cilegon Steel', 'Channel', '4 inch', 68.90, 'kg');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `customer_id` varchar(255) NOT NULL,
  `customer_name` varchar(50) NOT NULL,
  `contact` varchar(50) NOT NULL,
  `address` text NOT NULL,
  `email` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`id`, `customer_id`, `customer_name`, `contact`, `address`, `email`) VALUES
(1, 'CUST-001', 'PT Pembangunan Jaya', '021-5551234', 'Jl. Industri Raya No. 1, Jakarta', 'contact@pembangunanjaya.com'),
(2, 'CUST-002', 'CV Maju Bersama', '022-5555678', 'Jl. Sudirman No. 123, Bandung', 'info@majubersama.com'),
(3, 'CUST-003', 'PT Konstruksi Andalan', '031-5559876', 'Jl. Pahlawan No. 45, Surabaya', 'sales@konstruksiandalan.com'),
(4, 'CUST-004', 'CV Karya Mandiri', '024-5554321', 'Jl. Gajah Mada No. 67, Semarang', 'info@karyamandiri.com'),
(5, 'CUST-005', 'PT Bangun Sejahtera', '061-5558765', 'Jl. Diponegoro No. 89, Medan', 'contact@bangunsejahtera.com'),
(6, 'CUST-006', 'CV Cipta Karya', '0411-5552468', 'Jl. Veteran No. 12, Makassar', 'info@ciptakarya.com'),
(7, 'CUST-007', 'PT Baja Makmur', '0721-5551357', 'Jl. Raya Kedaton No. 34, Lampung', 'sales@bajamakmur.com'),
(8, 'CUST-008', 'CV Sukses Abadi', '0751-5553690', 'Jl. Bagindo Aziz No. 56, Padang', 'contact@suksesabadi.com'),
(9, 'CUST-009', 'PT Proyek Nusantara', '0561-5551470', 'Jl. Tanjungpura No. 78, Pontianak', 'info@proyeknusantara.com'),
(10, 'CUST-010', 'CV Mitra Utama', '0361-5552580', 'Jl. Bypass Ngurah Rai No. 90, Denpasar', 'sales@mitrautama.com');

-- --------------------------------------------------------

--
-- Table structure for table `purchasing`
--

CREATE TABLE `purchasing` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `purchase_date` date NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `stock_id` bigint(20) UNSIGNED NOT NULL,
  `supplier_id` bigint(20) UNSIGNED NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `sub_total` decimal(10,2) NOT NULL,
  `price_total` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `brand` varchar(50) NOT NULL,
  `type` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `purchasing`
--

INSERT INTO `purchasing` (`id`, `purchase_date`, `invoice_number`, `stock_id`, `supplier_id`, `quantity`, `price`, `sub_total`, `price_total`, `total_price`, `brand`, `type`) VALUES
(21, '2025-01-16', 'BLI-20250116-674339-5630', 4, 2, 2, 750000.00, 1500000.00, 1500000.00, 1500000.00, 'Ispat Indo', 'Tube'),
(22, '2025-01-16', 'BLI-20250116-674339-5630', 6, 5, 6, 950000.00, 5700000.00, 5700000.00, 5700000.00, 'Steel Pipe Industry', 'Pipe'),
(23, '2025-01-16', 'BLI-20250116-674339-5630', 8, 4, 8, 450000.00, 3600000.00, 3600000.00, 3600000.00, 'Jaya Steel', 'Wire'),
(24, '2025-01-16', 'BLI-20250116-674339-5630', 6, 4, 7, 950000.00, 6650000.00, 6650000.00, 6650000.00, 'Steel Pipe Industry', 'Pipe'),
(25, '2025-01-16', 'BLI-20250116-674339-5630', 10, 2, 8, 890000.00, 7120000.00, 7120000.00, 7120000.00, 'Cilegon Steel', 'Channel'),
(26, '2025-01-16', 'BLI-20250116-674339-5630', 3, 2, 3, 2500000.00, 7500000.00, 7500000.00, 7500000.00, 'Master Steel', 'Beam'),
(27, '2025-01-16', 'BLI-20250116-674339-5630', 2, 3, 5, 850000.00, 4250000.00, 4250000.00, 4250000.00, 'Gunung Steel', 'Rod'),
(28, '2025-01-16', 'BLI-20250116-674339-5630', 5, 4, 12, 1800000.00, 21600000.00, 21600000.00, 21600000.00, 'Gunawan Dianjaya Steel', 'Plate');

-- --------------------------------------------------------

--
-- Table structure for table `returns`
--

CREATE TABLE `returns` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `return_date` date NOT NULL,
  `return_id` varchar(255) NOT NULL,
  `return_type` varchar(15) NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `reason` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `returns`
--

INSERT INTO `returns` (`id`, `return_date`, `return_id`, `return_type`, `invoice_number`, `reason`) VALUES
(11, '2025-01-16', 'RET-441', 'Beli', 'BLI-20250116-674339-5630', 'Barang Tidak Sesuai'),
(12, '2025-01-16', 'RET-773', 'Beli', 'BLI-20250116-490572-9554', 'Salah Pengiriman'),
(13, '2025-01-16', 'RET-824', 'Jual', 'JUL-20250116-752076-5992', 'Salah Pengiriman'),
(14, '2025-01-08', 'RET-018', 'Jual', 'JUL-20250116-752076-5992', 'Barang Rusak'),
(15, '2025-01-03', 'RET-542', 'Beli', 'BLI-20250116-674339-5630', 'Tidak Sesuai'),
(16, '2025-01-07', 'RET-148', 'Jual', 'JUL-20250116-752076-5992', 'Salah Pengiriman'),
(17, '2025-01-10', 'RET-715', 'Beli', 'BLI-20250113-662277-5031', 'Tidak Sesuai'),
(18, '2025-01-14', 'RET-523', 'Jual', 'JUL-20250113-782533-3507', 'Barang Rusak');

-- --------------------------------------------------------

--
-- Table structure for table `sales`
--

CREATE TABLE `sales` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `sale_date` date NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `stock_id` bigint(20) UNSIGNED NOT NULL,
  `customer_id` bigint(20) UNSIGNED NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `sub_total` decimal(10,2) NOT NULL,
  `price_total` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `brand` varchar(50) NOT NULL,
  `type` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sales`
--

INSERT INTO `sales` (`id`, `sale_date`, `invoice_number`, `stock_id`, `customer_id`, `quantity`, `price`, `sub_total`, `price_total`, `total_price`, `brand`, `type`) VALUES
(12, '2025-01-16', 'JUL-20250116-752076-5992', 3, 3, 7, 3000000.00, 21000000.00, 21000000.00, 21000000.00, 'Master Steel', 'Beam'),
(13, '2025-01-16', 'JUL-20250116-752076-5992', 2, 7, 9, 1000000.00, 9000000.00, 9000000.00, 9000000.00, 'Gunung Steel', 'Rod'),
(14, '2025-01-16', 'JUL-20250116-752076-5992', 7, 5, 8, 3800000.00, 30400000.00, 30400000.00, 30400000.00, 'Jayapari Steel', 'Coil'),
(15, '2025-01-16', 'JUL-20250116-752076-5992', 2, 4, 5, 1000000.00, 5000000.00, 5000000.00, 5000000.00, 'Gunung Steel', 'Rod'),
(16, '2025-01-16', 'JUL-20250116-752076-5992', 6, 5, 8, 1150000.00, 9200000.00, 9200000.00, 9200000.00, 'Steel Pipe Industry', 'Pipe'),
(17, '2025-01-16', 'JUL-20250116-752076-5992', 8, 2, 3, 550000.00, 1650000.00, 1650000.00, 1650000.00, 'Jaya Steel', 'Wire'),
(18, '2025-01-16', 'JUL-20250116-752076-5992', 8, 2, 7, 550000.00, 3850000.00, 3850000.00, 3850000.00, 'Jaya Steel', 'Wire'),
(19, '2025-01-16', 'JUL-20250116-752076-5992', 6, 3, 3, 1150000.00, 3450000.00, 3450000.00, 3450000.00, 'Steel Pipe Industry', 'Pipe');

-- --------------------------------------------------------

--
-- Table structure for table `stocks`
--

CREATE TABLE `stocks` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `stock_id` varchar(255) NOT NULL,
  `category_id` bigint(20) UNSIGNED NOT NULL,
  `quantity` int(11) NOT NULL,
  `purchase_price` decimal(10,2) NOT NULL,
  `selling_price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `stocks`
--

INSERT INTO `stocks` (`id`, `stock_id`, `category_id`, `quantity`, `purchase_price`, `selling_price`) VALUES
(1, 'STK-001', 1, 102, 1500000.00, 1800000.00),
(2, 'STK-002', 2, 153, 850000.00, 1000000.00),
(3, 'STK-003', 3, 76, 2500000.00, 3000000.00),
(4, 'STK-004', 4, 122, 750000.00, 900000.00),
(5, 'STK-005', 5, 102, 1800000.00, 2200000.00),
(6, 'STK-006', 6, 102, 950000.00, 1150000.00),
(7, 'STK-007', 7, 68, 3200000.00, 3800000.00),
(8, 'STK-008', 8, 178, 450000.00, 550000.00),
(9, 'STK-009', 9, 95, 680000.00, 820000.00),
(10, 'STK-010', 10, 144, 890000.00, 1050000.00);

-- --------------------------------------------------------

--
-- Table structure for table `suppliers`
--

CREATE TABLE `suppliers` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `supplier_id` varchar(255) NOT NULL,
  `supplier_name` varchar(50) NOT NULL,
  `contact` varchar(50) NOT NULL,
  `address` text NOT NULL,
  `email` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `suppliers`
--

INSERT INTO `suppliers` (`id`, `supplier_id`, `supplier_name`, `contact`, `address`, `email`) VALUES
(1, 'SUP-001', 'PT Baja Prima', '021-5562468', 'Jl. Industri 5 No. 10, Cikarang', 'purchasing@bajaprima.com'),
(2, 'SUP-002', 'CV Logam Jaya', '031-5561357', 'Jl. Rungkut Industri No. 15, Surabaya', 'order@logamjaya.com1'),
(3, 'SUP-003', 'PT Material Sukses', '022-5563690', 'Jl. Soekarno-Hatta No. 88, Bandung', 'sales@materialsukses.com'),
(4, 'SUP-004', 'CV Makmur Steel', '024-5561470', 'Jl. Siliwangi No. 45, Semarang', 'purchase@makmursteel.com'),
(5, 'SUP-005', 'PT Surya Logam', '061-5562580', 'Jl. Gatot Subroto No. 123, Medan', 'order@suryalogam.com'),
(6, 'SUP-006', 'CV Mitra Baja', '0411-5564680', 'Jl. Perintis No. 67, Makassar', 'sales@mitrabaja.com'),
(7, 'SUP-007', 'PT Mega Steel', '0721-5563579', 'Jl. Sudirman No. 89, Lampung', 'purchasing@megasteel.com'),
(8, 'SUP-008', 'CV Sentosa Logam', '0751-5562468', 'Jl. Rasuna Said No. 34, Padang', 'order@sentosalogam.com'),
(9, 'SUP-009', 'PT Abadi Metal', '0561-5561357', 'Jl. Ahmad Yani No. 56, Pontianak', 'sales@abadimetal.com'),
(10, 'SUP-010', 'CV Baja Mandiri', '0361-5563690', 'Jl. Sunset Road No. 78, Denpasar', 'purchase@bajamandiri.com');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `username` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `name` varchar(50) NOT NULL,
  `password_hash` varchar(128) NOT NULL,
  `salt` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `name`, `password_hash`, `salt`, `created_at`, `updated_at`) VALUES
(7, 'admin', 'admin@gmail.com', 'admin', '$2a$10$TjAZyNTZNk1/TBwTIK.uZ.c6eOmcllbWC1dz2Z5EHnpv7Hv6VH.lK', '5b457889-fcda-44da-ac0e-587c07c2', '2025-01-16 16:26:04', '2025-01-16 16:26:04');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `category_id` (`category_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `customer_id` (`customer_id`);

--
-- Indexes for table `purchasing`
--
ALTER TABLE `purchasing`
  ADD PRIMARY KEY (`id`),
  ADD KEY `stock_id` (`stock_id`),
  ADD KEY `supplier_id` (`supplier_id`);

--
-- Indexes for table `returns`
--
ALTER TABLE `returns`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `return_id` (`return_id`),
  ADD KEY `invoice_number` (`invoice_number`);

--
-- Indexes for table `sales`
--
ALTER TABLE `sales`
  ADD PRIMARY KEY (`id`),
  ADD KEY `stock_id` (`stock_id`),
  ADD KEY `customer_id` (`customer_id`);

--
-- Indexes for table `stocks`
--
ALTER TABLE `stocks`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `stock_id` (`stock_id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `suppliers`
--
ALTER TABLE `suppliers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `supplier_id` (`supplier_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `purchasing`
--
ALTER TABLE `purchasing`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `returns`
--
ALTER TABLE `returns`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `sales`
--
ALTER TABLE `sales`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `stocks`
--
ALTER TABLE `stocks`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `suppliers`
--
ALTER TABLE `suppliers`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `purchasing`
--
ALTER TABLE `purchasing`
  ADD CONSTRAINT `purchasing_ibfk_1` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`),
  ADD CONSTRAINT `purchasing_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`);

--
-- Constraints for table `sales`
--
ALTER TABLE `sales`
  ADD CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`),
  ADD CONSTRAINT `sales_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`);

--
-- Constraints for table `stocks`
--
ALTER TABLE `stocks`
  ADD CONSTRAINT `stocks_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
