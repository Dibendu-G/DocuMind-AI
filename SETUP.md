# DocuMind AI - Quick Setup Guide

**Developed by:** DEV. DIBENDU

## Prerequisites

✅ **Java 17** - Already available on your system
- Version: `java version "17.0.10" 2024-01-16 LTS`

❌ **Maven** - Not found in PATH

🗄️ **MySQL Database** - Required for production use
- See `DATABASE_SETUP.md` for complete MySQL configuration

## Option 1: Install Maven (Recommended)

### Windows Installation:
1. Download Apache Maven from: https://maven.apache.org/download.cgi
   - Get the Binary zip archive (e.g., `apache-maven-3.9.6-bin.zip`)

2. Extract to a folder:
   ```
   C:\apache-maven-3.9.6\
   ```

3. Add Maven to PATH:
   - Open System Properties → Environment Variables
   - Add to PATH: `C:\apache-maven-3.9.6\bin`
   - Restart command prompt

4. Verify installation:
   ```bash
   mvn --version
   ```

5. Run the project:
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

## Option 2: Use IDE (Easy Alternative)

### IntelliJ IDEA:
1. Open the project folder
2. IntelliJ will automatically detect the Maven project
3. Wait for dependencies to download
4. Run `OcrSystemApplication.java`

### Eclipse:
1. File → Import → Existing Maven Projects
2. Select the project folder
3. Wait for dependencies to download
4. Right-click project → Run As → Spring Boot App

### VS Code:
1. Install Java Extension Pack
2. Open the project folder
3. Install Maven for Java extension
4. Use Ctrl+Shift+P → "Java: Run"

## Option 3: Use Maven Wrapper (if available)

If Maven wrapper is configured:
```bash
./mvnw clean compile
./mvnw spring-boot:run
```

## Database Setup (Required)

**DocuMind AI** uses MySQL for production. Quick setup options:

### Option 1: Default MySQL Setup
```bash
# Use default MySQL root user
DB_USERNAME=root
DB_PASSWORD=password
```

### Option 2: Environment Variables (Recommended)
```bash
# Set your MySQL credentials
set DB_USERNAME=your_mysql_username
set DB_PASSWORD=your_mysql_password
```

**Database:** `documind_ai` (created automatically)
**See:** `DATABASE_SETUP.md` for detailed MySQL configuration

## Quick Test

Once running, test the application:

1. **Health Check**: 
   ```
   GET http://localhost:8080/api/ocr/health
   ```

2. **Database Status**: Check if MySQL connection is working
   ```json
   {
     "components": {
       "database": "UP",
       "opencv": "UP"
     }
   }
   ```

3. **System Stats**:
   ```
   GET http://localhost:8080/api/ocr/stats
   ```

## Project Structure

```
DocuMind AI Platform/
├── src/
│   ├── main/
│   │   ├── java/com/inhouse/ocr/
│   │   │   ├── OcrSystemApplication.java    # Main application
│   │   │   ├── controller/                  # REST APIs
│   │   │   ├── service/                     # Business logic
│   │   │   ├── entity/                      # Database entities
│   │   │   ├── repository/                  # Data access
│   │   │   ├── dto/                         # API responses
│   │   │   └── config/                      # Configuration
│   │   └── resources/
│   │       └── application.yml              # App configuration
│   └── test/                                # Unit tests
├── pom.xml                                  # Maven dependencies
├── README.md                                # Full documentation
├── DATABASE_SETUP.md                       # MySQL setup guide
└── run.bat                                  # Windows runner script
```

## Next Steps

Once the application is running:

1. **Test PDF Upload**: Use Postman or curl to upload a PDF
2. **Monitor Processing**: Check job status via API
3. **View Results**: Get extracted text (placeholder for now)
4. **Check Logs**: Monitor console output for processing details

## Phase 1 Status

✅ **Complete Infrastructure**:
- Spring Boot application with all dependencies
- RESTful API endpoints
- PDF to image conversion
- Image preprocessing with OpenCV
- Database layer with job tracking
- Async processing pipeline
- Health monitoring

🔄 **Ready for Phase 2**:
- Neural network integration (DeepLearning4J)
- Advanced OCR algorithms
- Training pipeline implementation

## Troubleshooting

### Common Issues:

1. **Port 8080 in use**: Change port in `application.yml`:
   ```yaml
   server:
     port: 8081
   ```

2. **OutOfMemory**: Increase JVM heap:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx4g"
   ```

3. **OpenCV not loading**: Check logs for OpenCV status in health endpoint

4. **File upload fails**: Ensure temp directory is writable

Need help? Check the detailed README.md for complete documentation.
