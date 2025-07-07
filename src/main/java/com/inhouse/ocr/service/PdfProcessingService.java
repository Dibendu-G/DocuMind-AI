package com.inhouse.ocr.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class PdfProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfProcessingService.class);
    private static final float DEFAULT_DPI = 300f; // High DPI for better OCR accuracy
    
    /**
     * Converts all pages of a PDF to BufferedImages
     */
    public List<BufferedImage> convertPdfToImages(File pdfFile) throws IOException {
        return convertPdfToImages(pdfFile, DEFAULT_DPI);
    }
    
    /**
     * Converts all pages of a PDF to BufferedImages with specified DPI
     */
    public List<BufferedImage> convertPdfToImages(File pdfFile, float dpi) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int numberOfPages = document.getNumberOfPages();
            
            logger.info("Converting PDF with {} pages to images at {} DPI", numberOfPages, dpi);
            
            for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
                try {
                    BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
                    images.add(image);
                    logger.debug("Converted page {} to image ({}x{})", 
                               pageIndex + 1, image.getWidth(), image.getHeight());
                } catch (IOException e) {
                    logger.error("Failed to convert page {} to image", pageIndex + 1, e);
                    throw new IOException("Failed to convert page " + (pageIndex + 1) + " to image", e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load PDF document: {}", pdfFile.getAbsolutePath(), e);
            throw new IOException("Failed to load PDF document", e);
        }
        
        return images;
    }
    
    /**
     * Converts a single page of a PDF to BufferedImage
     */
    public BufferedImage convertPdfPageToImage(File pdfFile, int pageIndex) throws IOException {
        return convertPdfPageToImage(pdfFile, pageIndex, DEFAULT_DPI);
    }
    
    /**
     * Converts a single page of a PDF to BufferedImage with specified DPI
     */
    public BufferedImage convertPdfPageToImage(File pdfFile, int pageIndex, float dpi) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            
            if (pageIndex >= document.getNumberOfPages()) {
                throw new IllegalArgumentException("Page index " + pageIndex + " is out of bounds. PDF has " + 
                                                 document.getNumberOfPages() + " pages.");
            }
            
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
            logger.debug("Converted page {} to image ({}x{}) at {} DPI", 
                       pageIndex + 1, image.getWidth(), image.getHeight(), dpi);
            
            return image;
        } catch (IOException e) {
            logger.error("Failed to convert page {} of PDF: {}", pageIndex + 1, pdfFile.getAbsolutePath(), e);
            throw new IOException("Failed to convert page " + (pageIndex + 1) + " of PDF", e);
        }
    }
    
    /**
     * Gets the number of pages in a PDF
     */
    public int getPageCount(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int pageCount = document.getNumberOfPages();
            logger.debug("PDF {} has {} pages", pdfFile.getName(), pageCount);
            return pageCount;
        } catch (IOException e) {
            logger.error("Failed to get page count for PDF: {}", pdfFile.getAbsolutePath(), e);
            throw new IOException("Failed to get page count for PDF", e);
        }
    }
    
    /**
     * Saves BufferedImages to temporary files and returns the file paths
     */
    public List<File> saveImagesToTempFiles(List<BufferedImage> images, String tempDir, String jobId) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        File tempDirectory = new File(tempDir);
        
        if (!tempDirectory.exists()) {
            tempDirectory.mkdirs();
        }
        
        for (int i = 0; i < images.size(); i++) {
            String filename = String.format("%s_page_%03d.png", jobId, i + 1);
            File tempFile = new File(tempDirectory, filename);
            
            try {
                ImageIO.write(images.get(i), "PNG", tempFile);
                tempFiles.add(tempFile);
                logger.debug("Saved page {} image to: {}", i + 1, tempFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to save page {} image to file", i + 1, e);
                // Clean up any files created so far
                tempFiles.forEach(file -> {
                    if (file.exists()) {
                        file.delete();
                    }
                });
                throw new IOException("Failed to save page " + (i + 1) + " image to file", e);
            }
        }
        
        return tempFiles;
    }
    
    /**
     * Validates if the file is a valid PDF
     */
    public boolean isValidPdf(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return document.getNumberOfPages() > 0;
        } catch (Exception e) {
            logger.warn("Invalid PDF file: {}", pdfFile.getAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Gets PDF metadata information
     */
    public PdfInfo getPdfInfo(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PdfInfo info = new PdfInfo();
            info.setPageCount(document.getNumberOfPages());
            info.setFileSize(pdfFile.length());
            info.setFileName(pdfFile.getName());
            
            if (document.getDocumentInformation() != null) {
                info.setTitle(document.getDocumentInformation().getTitle());
                info.setAuthor(document.getDocumentInformation().getAuthor());
                info.setSubject(document.getDocumentInformation().getSubject());
            }
            
            return info;
        } catch (IOException e) {
            logger.error("Failed to get PDF info for: {}", pdfFile.getAbsolutePath(), e);
            throw new IOException("Failed to get PDF information", e);
        }
    }
    
    /**
     * PDF information holder class
     */
    public static class PdfInfo {
        private int pageCount;
        private long fileSize;
        private String fileName;
        private String title;
        private String author;
        private String subject;
        
        // Getters and Setters
        public int getPageCount() { return pageCount; }
        public void setPageCount(int pageCount) { this.pageCount = pageCount; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
    }
}
