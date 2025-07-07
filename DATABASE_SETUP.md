# DocuMind AI - MySQL Database Setup Guide

**Developed by:** DEV. DIBENDU

## 🗄️ Database Configuration

DocuMind AI platform is configured to use **MySQL** as the primary database with automatic schema management and connection pooling.

## 📋 Prerequisites

### 1. MySQL Installation
- **MySQL Server 8.0+** (recommended)
- **MySQL Workbench** (for database management)
- **Admin access** to create databases and users

### 2. Required Information
- **Host**: `localhost` (default) or your MySQL server address
- **Port**: `3306` (default MySQL port)
- **Database Name**: `documind_ai` (will be created automatically)
- **Username**: Configurable via environment variables
- **Password**: Configurable via environment variables

## 🚀 Quick Setup Options

### Option 1: Default Configuration (Easiest)
```yaml
# Uses default MySQL root user
Username: root
Password: password
Database: documind_ai (auto-created)
```

### Option 2: Environment Variables (Recommended)
```bash
# Set environment variables
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password

# Then run the application
mvn spring-boot:run
```

### Option 3: Custom Configuration
Edit `application.yml` to match your MySQL setup.

## 🛠️ MySQL Workbench Setup

### Step 1: Create Database User (Recommended)
```sql
-- Connect to MySQL as root user
-- Create dedicated user for DocuMind AI
CREATE USER 'documind_user'@'localhost' IDENTIFIED BY 'secure_password';

-- Grant necessary privileges
GRANT ALL PRIVILEGES ON documind_ai.* TO 'documind_user'@'localhost';

-- Refresh privileges
FLUSH PRIVILEGES;
```

### Step 2: Verify Connection
```sql
-- Test connection with new user
-- Database will be created automatically when application starts
SHOW DATABASES;
```

### Step 3: Application Configuration
Update your environment variables or `application.yml`:
```yaml
spring:
  datasource:
    username: documind_user
    password: secure_password
```

## 📊 Database Schema

The application automatically creates the following tables:

### `ocr_jobs` Table
```sql
CREATE TABLE ocr_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(255) UNIQUE NOT NULL,
    original_filename VARCHAR(500),
    file_path VARCHAR(1000),
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'),
    created_at DATETIME,
    started_at DATETIME,
    completed_at DATETIME,
    error_message VARCHAR(1000),
    total_pages INT,
    processed_pages INT DEFAULT 0,
    overall_confidence DOUBLE,
    extracted_text LONGTEXT,
    processing_time_ms BIGINT,
    INDEX idx_job_id (job_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

## 🔧 Configuration Details

### Connection Pool Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Max connections
      minimum-idle: 5            # Min idle connections
      connection-timeout: 20000  # 20 seconds
      idle-timeout: 300000       # 5 minutes
```

### JPA/Hibernate Settings
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update           # Auto-update schema
    show-sql: false              # Set to true for debugging
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
```

## 🏥 Health Monitoring

The application monitors database health:

```bash
# Check database connectivity
GET http://localhost:8080/api/ocr/health

# Response includes database status
{
  "components": {
    "database": "UP",
    "opencv": "UP",
    ...
  }
}
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Connection Refused
```
Error: Connection refused to localhost:3306
```
**Solution:**
- Ensure MySQL server is running
- Check if port 3306 is available
- Verify MySQL service status

#### 2. Authentication Failed
```
Error: Access denied for user 'root'@'localhost'
```
**Solution:**
- Verify username/password in configuration
- Check MySQL user privileges
- Reset MySQL root password if needed

#### 3. Database Creation Failed
```
Error: Unknown database 'documind_ai'
```
**Solution:**
- Ensure `createDatabaseIfNotExist=true` in URL
- Create database manually if needed:
  ```sql
  CREATE DATABASE documind_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

#### 4. Schema Update Issues
```
Error: Table 'ocr_jobs' doesn't exist
```
**Solution:**
- Check `hibernate.ddl-auto` setting
- Verify database user has CREATE/ALTER privileges
- Manually run schema creation if needed

### MySQL Workbench Verification

1. **Connect to Database:**
   ```
   Host: localhost
   Port: 3306
   Username: documind_user (or root)
   Password: [your_password]
   ```

2. **Verify Database:**
   ```sql
   USE documind_ai;
   SHOW TABLES;
   DESCRIBE ocr_jobs;
   ```

3. **Monitor Jobs:**
   ```sql
   SELECT job_id, status, created_at, total_pages 
   FROM ocr_jobs 
   ORDER BY created_at DESC 
   LIMIT 10;
   ```

## 🔐 Security Best Practices

### 1. User Management
```sql
-- Create application-specific user
CREATE USER 'documind_app'@'localhost' IDENTIFIED BY 'strong_random_password';

-- Grant minimal required privileges
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER ON documind_ai.* TO 'documind_app'@'localhost';
```

### 2. Connection Security
```yaml
spring:
  datasource:
    # Enable SSL for production
    url: jdbc:mysql://localhost:3306/documind_ai?useSSL=true&requireSSL=true
```

### 3. Environment Variables
```bash
# Never hardcode passwords in configuration files
export DB_USERNAME=documind_app
export DB_PASSWORD=your_secure_password_here
```

## 📈 Performance Optimization

### 1. Indexing Strategy
```sql
-- Automatically created by JPA, but manual optimization possible
CREATE INDEX idx_status_created ON ocr_jobs(status, created_at);
CREATE INDEX idx_processing_time ON ocr_jobs(processing_time_ms);
```

### 2. Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      # Adjust based on your server capacity
      maximum-pool-size: 30
      minimum-idle: 10
      # Tune for your network latency
      connection-timeout: 30000
```

### 3. Database Monitoring
```sql
-- Monitor connection usage
SHOW PROCESSLIST;

-- Check database size
SELECT 
    table_schema as 'Database', 
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as 'Size (MB)' 
FROM information_schema.tables 
WHERE table_schema = 'documind_ai';
```

## 🚀 Production Deployment

### 1. Database Server Setup
- Use dedicated MySQL server
- Configure proper backup strategy
- Set up monitoring and alerting
- Implement connection pooling

### 2. Configuration Management
```yaml
# Production configuration
spring:
  datasource:
    url: jdbc:mysql://mysql-server:3306/documind_ai_prod?useSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-modify schema in production
```

### 3. Backup Strategy
```bash
# Daily backup script
mysqldump -u documind_user -p documind_ai > backup_$(date +%Y%m%d).sql

# Automated backup with retention
find /backup/path -name "backup_*.sql" -mtime +30 -delete
```

---

## 📞 Quick Start Summary

1. **Install MySQL Server & Workbench**
2. **Create database user** (optional but recommended)
3. **Set environment variables:**
   ```bash
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   ```
4. **Start the application** - database will be created automatically
5. **Verify connection** via health endpoint

**Database URL:** `jdbc:mysql://localhost:3306/documind_ai`  
**Auto-creation:** Enabled  
**Schema management:** Automatic via Hibernate  

The DocuMind AI platform is now ready to store and manage all OCR processing jobs in MySQL!
