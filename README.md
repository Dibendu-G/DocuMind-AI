# DocuMind AI - Enterprise OCR Platform

**Developed by:** DEV. DIBENDU

A complete enterprise-grade OCR platform built with Java Spring Boot that processes PDFs (handwritten, printed, and blurry) with maximum accuracy while maintaining complete data privacy through local processing.

## 🚀 Project Overview

**DocuMind AI** is an enterprise-grade OCR platform engineered to achieve 95%+ accuracy on various document types while maintaining complete data privacy through local processing. The system employs a sophisticated multi-stage pipeline: PDF → Image → Text Detection → Recognition → Post-processing.

**Developer & Ownership:** DEV. DIBENDU - Backend development, architecture design, implementation, and neural network integration

## 📋 Technical Stack

- **Backend**: Java 17, Spring Boot 3.1.5
- **ML Framework**: DeepLearning4J (pure Java implementation)
- **Image Processing**: OpenCV Java bindings
- **PDF Handling**: Apache PDFBox
- **Database**: MySQL 8.0+ with automatic schema management
- **Build Tool**: Maven

## 🏗️ Architecture

### Current Phase (Phase 1): Foundation
- ✅ Spring Boot project setup with all dependencies
- ✅ RESTful APIs for file upload and processing
- ✅ PDF to image conversion pipeline
- ✅ Basic image preprocessing with OpenCV
- ✅ Database layer with job tracking
- ✅ Async processing capabilities
- ✅ Health monitoring and statistics

### Upcoming Phases
- **Phase 2**: Advanced image preprocessing (deblur, denoise, deskew)
- **Phase 3**: Neural network implementation (CRNN architecture)
- **Phase 4**: Training pipeline with synthetic data generation
- **Phase 5**: Performance optimization and caching
- **Phase 6**: Comprehensive testing and accuracy benchmarking

## 🛠️ Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL Server 8.0+ with MySQL Workbench
- At least 4GB RAM recommended

### Installation

1. **Clone and navigate to the project**:
   ```bash
   cd C:\YOURPATH
   ```

2. **Setup MySQL Database**:
   ```bash
   # See DATABASE_SETUP.md for complete guide
   # Quick setup: Set environment variables
   set DB_USERNAME=your_mysql_username
   set DB_PASSWORD=your_mysql_password
   ```

3. **Build the project**:
   ```bash
   mvn clean install
   ```

4. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

5. **Verify the installation**:
   - Application will start on `http://localhost:8080`
   - Health check: `GET http://localhost:8080/api/ocr/health`
   - Database: `documind_ai` (created automatically in MySQL)
   - Complete database setup guide: See `DATABASE_SETUP.md`

## 📡 API Endpoints

### Core OCR Operations

#### Upload PDF for Processing
```http
POST /api/ocr/upload
Content-Type: multipart/form-data
Body: file (PDF file)
```

**Response**:
```json
{
  "jobId": "OCR_1640123456789_abc12345",
  "message": "File uploaded successfully. Processing started.",
  "status": "PENDING"
}
```

#### Check Job Status
```http
GET /api/ocr/status/{jobId}
```

**Response**:
```json
{
  "jobId": "OCR_1640123456789_abc12345",
  "originalFilename": "document.pdf",
  "status": "PROCESSING",
  "totalPages": 5,
  "processedPages": 3,
  "progressPercentage": 60.0,
  "createdAt": "2024-01-01 10:00:00",
  "startedAt": "2024-01-01 10:00:15"
}
```

#### Get OCR Results
```http
GET /api/ocr/result/{jobId}
```

**Response**:
```json
{
  "jobId": "OCR_1640123456789_abc12345",
  "status": "COMPLETED",
  "extractedText": "Full extracted text from all pages...",
  "overallConfidence": 0.89,
  "processingTimeMs": 15000,
  "pageResults": [
    {
      "pageNumber": 1,
      "extractedText": "Page 1 content...",
      "confidence": 0.92
    }
  ]
}
```

### Batch Operations

#### Batch Upload
```http
POST /api/ocr/batch
Content-Type: multipart/form-data
Body: files[] (multiple PDF files, max 10)
```

#### Batch Status
```http
GET /api/ocr/batch/status?jobIds=job1,job2,job3
```

### Monitoring

#### System Statistics
```http
GET /api/ocr/stats
```

#### Health Check
```http
GET /api/ocr/health
```

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

The tests verify:
- Database connectivity
- Service layer functionality
- Basic image processing capabilities
- Job status transitions

## 📊 Current Capabilities

### ✅ Implemented Features
- **PDF Processing**: Convert PDF pages to high-resolution images (300 DPI)
- **Image Preprocessing**: 
  - Grayscale conversion
  - Noise reduction using fastNlMeansDenoising
  - Contrast enhancement with CLAHE
  - Image deskewing using Hough line detection
  - Image sharpening with unsharp masking
  - Adaptive binarization
- **Quality Assessment**: Automatic image quality scoring
- **Async Processing**: Non-blocking job processing with progress tracking
- **File Management**: Automatic cleanup of temporary files
- **Database Tracking**: Complete job history and statistics
- **RESTful APIs**: Comprehensive API set with proper error handling

### 🔄 In Progress (Phase 1 Foundation)
- Basic OCR placeholder (returns structured sample text)
- System health monitoring
- Batch processing capabilities

### 🎯 Planned Features (Future Phases)
- **Neural Network OCR**: CRNN architecture for high accuracy
- **Text Detection**: YOLO-style text region detection
- **Handwriting Recognition**: Specialized models for handwritten text
- **Training Pipeline**: Automated model training with synthetic data
- **Advanced Post-processing**: Spell checking and text correction
- **Performance Optimization**: GPU acceleration and caching

## ⚙️ Configuration

Key configuration options in `application.yml`:

```yaml
ocr:
  processing:
    temp-directory: ${java.io.tmpdir}/ocr-temp
    max-concurrent-jobs: 10
    timeout-minutes: 30
  
  image:
    max-width: 2048
    max-height: 2048
    quality-threshold: 0.7
  
  performance:
    batch-size: 32
    cache-enabled: true
```

## 🔧 Development Guidelines

### Adding New Features
1. Follow the existing package structure
2. Add comprehensive logging
3. Include unit tests
4. Update API documentation
5. Handle errors gracefully

### Code Quality Standards
- Use Spring Boot best practices
- Implement proper error handling
- Write comprehensive unit tests
- Document all public APIs
- Follow Java naming conventions

## 🎯 Performance Targets

| Metric | Target | Current Status       |
|--------|--------|----------------------|
| Printed Text Accuracy | 98-99% | OnHold (Phase 3)     |
| Handwritten Text Accuracy | 90-95% | OnHold (Phase 3)     |
| Blurry Text Accuracy | 85-90% | InProgress (Phase 3) |
| Processing Speed | 100+ pages/min | Infrastructure ready |
| Memory Usage | < 4GB RAM | Optimized            |

## 🛡️ Security & Privacy

- ✅ **No External APIs**: All processing done locally
- ✅ **Secure File Handling**: Temporary files with automatic cleanup
- ✅ **Data Encryption**: Ready for encryption at rest and in transit
- ✅ **Audit Logging**: Complete processing history tracking

## 📈 Monitoring & Metrics

The system tracks:
- Job processing statistics
- Average processing times
- Confidence scores
- System component health
- Error rates and patterns

## 🤝 Contributing

**This is an open-source project and contributions are welcome!**

We encourage developers worldwide to contribute to this project. However, please follow these guidelines:

### 🔄 Contribution Guidelines

1. **Branch Management**:
   - 🚫 **DO NOT** create pull requests directly to the `master` branch
   - ✅ **ALWAYS** create a separate feature branch for your contributions
   - Format: `feature/your-feature-name` or `bugfix/issue-description`

2. **Feature Development**:
   ```bash
   # Create a new feature branch
   git checkout -b feature/your-new-feature
   
   # Work on your feature
   # ... make changes ...
   
   # Commit your changes
   git add .
   git commit -m "Add: Description of your feature"
   
   # Push your feature branch
   git push origin feature/your-new-feature
   
   # Create PR to develop branch (NOT master)
   ```

3. **Bug Fixes and Hotfixes**:
   ```bash
   # Create a bugfix branch
   git checkout -b bugfix/issue-name
   
   # For critical hotfixes
   git checkout -b hotfix/critical-issue
   ```

4. **Code Quality Requirements**:
   - Follow existing code style and patterns
   - Add comprehensive unit tests
   - Update documentation if needed
   - Ensure all existing tests pass

5. **Pull Request Process**:
   - Create detailed PR description
   - Reference any related issues
   - Wait for code review and approval
   - Address any feedback promptly

### 🌍 Developer Location

We welcome contributions from developers worldwide. Contributors can be located anywhere and are encouraged to participate in this open-source project.

### 📋 Future Development Roadmap

Contributions are welcome in these areas:
1. **Neural Network Models**: CRNN architecture implementation
2. **Training Data Pipelines**: Synthetic data generation
3. **Advanced Preprocessing**: Deblur, denoise, deskew algorithms
4. **Performance Optimizations**: GPU acceleration, caching
5. **Accuracy Benchmarking**: Testing and validation tools
6. **UI/Frontend**: Web interface for the OCR system
7. **API Enhancements**: Additional endpoints and features
8. **Documentation**: Tutorials, guides, and examples

## ⚖️ Legal Notice and Copyright

**Original Author**: DEV. DIBENDU  
**Copyright**: © 2025 DEV. DIBENDU. All rights reserved.

### 🚨 Important Legal Requirements

1. **Attribution Required**: 
   - Any use, modification, or distribution must maintain proper attribution to the original author
   - Credit must be given to DEV. DIBENDU as the original creator

2. **No Unauthorized Replication**:
   - 🚫 **DO NOT** copy this entire project and claim it as your own work
   - 🚫 **DO NOT** remove or modify copyright notices
   - 🚫 **DO NOT** redistribute without proper attribution

3. **Contribution Terms**:
   - By contributing to this project, you agree that your contributions will be licensed under the same terms
   - You retain copyright to your contributions while granting usage rights to the project

4. **Commercial Use**:
   - Commercial use is permitted with proper attribution
   - Modified versions must clearly indicate changes made

### ⚖️ Legal Action Warning

**WARNING**: Any attempt to:
- Replicate this project without proper attribution
- Claim ownership of the original work
- Remove copyright notices
- Distribute modified versions without attribution

**May result in legal proceedings for copyright infringement.**

We take intellectual property rights seriously and will pursue legal action against unauthorized use or plagiarism.

### 📞 Contact for Legal Inquiries

For licensing questions or legal concerns, please contact the project maintainer through GitHub issues or discussions.

## 📄 License

This project is open-source software licensed under custom terms that require attribution to the original author. See the legal notice above for details.

---

**Note**: This is Phase 1 of the implementation. The foundation is complete and ready for neural network integration in Phase 3. The current version provides a fully functional OCR pipeline with placeholder text extraction that will be replaced with deep learning models.

**Developed with ❤️ by DEV. DIBENDU**
