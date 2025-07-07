package com.inhouse.ocr.service;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

@Service
public class ImagePreprocessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImagePreprocessingService.class);
    private static boolean openCvLoaded = false;
    
    static {
        try {
            OpenCV.loadLocally();
            openCvLoaded = true;
            logger.info("OpenCV loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load OpenCV", e);
        }
    }
    
    /**
     * Comprehensive image preprocessing pipeline
     */
    public BufferedImage preprocessImage(BufferedImage inputImage) {
        if (!openCvLoaded) {
            logger.warn("OpenCV not loaded, returning original image");
            return inputImage;
        }
        
        try {
            Mat originalMat = bufferedImageToMat(inputImage);
            Mat processedMat = new Mat();
            
            // 1. Convert to grayscale
            if (originalMat.channels() > 1) {
                Imgproc.cvtColor(originalMat, processedMat, Imgproc.COLOR_BGR2GRAY);
            } else {
                processedMat = originalMat.clone();
            }
            
            // 2. Noise reduction
            Mat denoisedMat = new Mat();
            Imgproc.fastNlMeansDenoising(processedMat, denoisedMat);
            
            // 3. Contrast enhancement using CLAHE
            Mat enhancedMat = enhanceContrast(denoisedMat);
            
            // 4. Deskew the image
            Mat deskewedMat = deskewImage(enhancedMat);
            
            // 5. Sharpen the image
            Mat sharpenedMat = sharpenImage(deskewedMat);
            
            // 6. Binarization using adaptive threshold
            Mat binaryMat = binarizeImage(sharpenedMat);
            
            BufferedImage result = matToBufferedImage(binaryMat);
            
            // Clean up memory
            originalMat.release();
            processedMat.release();
            denoisedMat.release();
            enhancedMat.release();
            deskewedMat.release();
            sharpenedMat.release();
            binaryMat.release();
            
            logger.debug("Image preprocessing completed successfully");
            return result;
            
        } catch (Exception e) {
            logger.error("Error during image preprocessing, returning original image", e);
            return inputImage;
        }
    }
    
    /**
     * Enhance image contrast using CLAHE (Contrast Limited Adaptive Histogram Equalization)
     */
    public Mat enhanceContrast(Mat inputMat) {
        Mat result = new Mat();
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(2.0);
        clahe.setTilesGridSize(new Size(8, 8));
        clahe.apply(inputMat, result);
        return result;
    }
    
    /**
     * Deskew image by detecting and correcting rotation
     */
    public Mat deskewImage(Mat inputMat) {
        try {
            // Find lines using Hough transform
            Mat edges = new Mat();
            Imgproc.Canny(inputMat, edges, 50, 150);
            
            Mat lines = new Mat();
            Imgproc.HoughLines(edges, lines, 1, Math.PI / 180, 100);
            
            if (lines.rows() == 0) {
                edges.release();
                lines.release();
                return inputMat.clone();
            }
            
            // Calculate average angle
            double angleSum = 0;
            int validLines = 0;
            
            for (int i = 0; i < lines.rows(); i++) {
                double[] line = lines.get(i, 0);
                double theta = line[1];
                
                // Convert to degrees and normalize
                double angleDeg = Math.toDegrees(theta) - 90;
                if (Math.abs(angleDeg) < 45) {
                    angleSum += angleDeg;
                    validLines++;
                }
            }
            
            if (validLines == 0) {
                edges.release();
                lines.release();
                return inputMat.clone();
            }
            
            double averageAngle = angleSum / validLines;
            
            // Rotate image if angle is significant
            if (Math.abs(averageAngle) > 0.5) {
                Mat rotated = rotateImage(inputMat, -averageAngle);
                edges.release();
                lines.release();
                return rotated;
            }
            
            edges.release();
            lines.release();
            return inputMat.clone();
            
        } catch (Exception e) {
            logger.warn("Failed to deskew image, returning original", e);
            return inputMat.clone();
        }
    }
    
    /**
     * Rotate image by specified angle
     */
    private Mat rotateImage(Mat inputMat, double angle) {
        Point center = new Point(inputMat.cols() / 2.0, inputMat.rows() / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        
        Mat rotated = new Mat();
        Imgproc.warpAffine(inputMat, rotated, rotationMatrix, inputMat.size(), 
                          Imgproc.INTER_CUBIC, Core.BORDER_REPLICATE, new Scalar(255));
        
        rotationMatrix.release();
        return rotated;
    }
    
    /**
     * Sharpen image using unsharp masking
     */
    public Mat sharpenImage(Mat inputMat) {
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(inputMat, blurred, new Size(3, 3), 0);
        
        Mat sharpened = new Mat();
        Core.addWeighted(inputMat, 1.5, blurred, -0.5, 0, sharpened);
        
        blurred.release();
        return sharpened;
    }
    
    /**
     * Binarize image using adaptive thresholding
     */
    public Mat binarizeImage(Mat inputMat) {
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(inputMat, binary, 255, 
                                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
                                Imgproc.THRESH_BINARY, 11, 2);
        return binary;
    }
    
    /**
     * Detect and correct blur in images
     */
    public BufferedImage enhanceBlurredImage(BufferedImage inputImage) {
        if (!openCvLoaded) {
            return inputImage;
        }
        
        try {
            Mat inputMat = bufferedImageToMat(inputImage);
            Mat gray = new Mat();
            
            if (inputMat.channels() > 1) {
                Imgproc.cvtColor(inputMat, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = inputMat.clone();
            }
            
            // Check blur level using Laplacian variance
            Mat laplacian = new Mat();
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
            
            Scalar mean = new Scalar();
            Scalar stddev = new Scalar();
            Core.meanStdDev(laplacian, mean, stddev);
            
            double variance = stddev.val[0] * stddev.val[0];
            logger.debug("Image blur variance: {}", variance);
            
            Mat enhanced;
            if (variance < 100) { // Image is blurry
                logger.debug("Applying deblurring to image");
                enhanced = deblurImage(gray);
            } else {
                enhanced = gray.clone();
            }
            
            BufferedImage result = matToBufferedImage(enhanced);
            
            // Clean up
            inputMat.release();
            gray.release();
            laplacian.release();
            enhanced.release();
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error enhancing blurred image", e);
            return inputImage;
        }
    }
    
    /**
     * Apply deblurring using unsharp masking
     */
    private Mat deblurImage(Mat inputMat) {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] kernelData = {
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
        };
        kernel.put(0, 0, kernelData);
        
        Mat deblurred = new Mat();
        Imgproc.filter2D(inputMat, deblurred, -1, kernel);
        
        kernel.release();
        return deblurred;
    }
    
    /**
     * Assess image quality
     */
    public double assessImageQuality(BufferedImage image) {
        if (!openCvLoaded) {
            return 1.0; // Default to good quality if OpenCV not available
        }
        
        try {
            Mat inputMat = bufferedImageToMat(image);
            Mat gray = new Mat();
            
            if (inputMat.channels() > 1) {
                Imgproc.cvtColor(inputMat, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = inputMat.clone();
            }
            
            // Calculate sharpness using Laplacian variance
            Mat laplacian = new Mat();
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
            
            Scalar mean = new Scalar();
            Scalar stddev = new Scalar();
            Core.meanStdDev(laplacian, mean, stddev);
            
            double sharpness = stddev.val[0] * stddev.val[0];
            
            // Normalize to 0-1 scale (higher is better)
            double quality = Math.min(sharpness / 1000.0, 1.0);
            
            inputMat.release();
            gray.release();
            laplacian.release();
            
            return quality;
            
        } catch (Exception e) {
            logger.error("Error assessing image quality", e);
            return 0.5; // Default to medium quality
        }
    }
    
    /**
     * Convert BufferedImage to OpenCV Mat
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            
            Mat mat = Imgcodecs.imdecode(new MatOfByte(imageInByte), Imgcodecs.IMREAD_COLOR);
            return mat;
        } catch (IOException e) {
            logger.error("Error converting BufferedImage to Mat", e);
            throw new RuntimeException("Failed to convert image", e);
        }
    }
    
    /**
     * Convert OpenCV Mat to BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, matOfByte);
            byte[] byteArray = matOfByte.toArray();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            BufferedImage bufferedImage = ImageIO.read(bais);
            bais.close();
            
            return bufferedImage;
        } catch (IOException e) {
            logger.error("Error converting Mat to BufferedImage", e);
            throw new RuntimeException("Failed to convert image", e);
        }
    }
    
    /**
     * Check if OpenCV is properly loaded
     */
    public boolean isOpenCvLoaded() {
        return openCvLoaded;
    }
}
