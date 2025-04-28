package com.sismics.util;

import com.sismics.BaseTest;
import com.sismics.docs.core.util.format.*;
import com.sismics.util.mime.MimeTypeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFileExtractionEdgeCases extends BaseTest {

    /**
     * Test empty file extraction.
     */
    @Test
    public void testEmptyFileExtraction() throws Exception {
        // Create temporary empty files
        Path emptyTxt = createEmptyFile("empty.txt");
        Path emptyPdf = createEmptyFile("empty.pdf");
        Path emptyDocx = createEmptyFile("empty.docx");
        
        // Test empty text file
        FormatHandler formatHandler = FormatHandlerUtil.find(MimeTypeUtil.guessMimeType(emptyTxt, "empty.txt"));
        Assert.assertNotNull(formatHandler);
        String content = formatHandler.extractContent("eng", emptyTxt);
        Assert.assertEquals("", content.trim());
        
        // Test invalid PDF file
        formatHandler = FormatHandlerUtil.find(MimeTypeUtil.guessMimeType(emptyPdf, "empty.pdf"));
        Assert.assertNotNull(formatHandler);
        try {
            content = formatHandler.extractContent("eng", emptyPdf);
            // If we reach here, the handler didn't throw an exception as expected
            // In this case, empty content is acceptable
            Assert.assertEquals("", content.trim());
        } catch (Exception e) {
            // Expected behavior for invalid PDF
            Assert.assertTrue(e.getMessage().contains("error") || 
                             e.getMessage().contains("invalid") ||
                             e.getMessage().contains("not a PDF file"));
        }
        
        // Test invalid DOCX file
        formatHandler = FormatHandlerUtil.find(MimeTypeUtil.guessMimeType(emptyDocx, "empty.docx"));
        Assert.assertNotNull(formatHandler);
        try {
            content = formatHandler.extractContent("eng", emptyDocx);
            // If we reach here, the handler didn't throw an exception as expected
            // In this case, empty content is acceptable
            Assert.assertEquals("", content.trim());
        } catch (Exception e) {
            // Expected behavior for invalid DOCX
            Assert.assertTrue(e.getMessage().contains("error") || 
                             e.getMessage().contains("invalid") ||
                             e.getMessage().contains("cannot be opened"));
        }
    }
    
    /**
     * Test extraction with unsupported languages.
     */
    @Test
    public void testUnsupportedLanguageExtraction() throws Exception {
        Path path = Paths.get(getResource(FILE_PDF).toURI());
        FormatHandler formatHandler = FormatHandlerUtil.find(MimeTypeUtil.guessMimeType(path, FILE_PDF));
        Assert.assertNotNull(formatHandler);
        Assert.assertTrue(formatHandler instanceof PdfFormatHandler);
        
        // Test with an unsupported language code
        String content = formatHandler.extractContent("xyz", path);
        Assert.assertFalse(content.isEmpty()); // Should still extract content despite unsupported language
        
        // Test with null language code (should default to something reasonable)
        content = formatHandler.extractContent(null, path);
        Assert.assertFalse(content.isEmpty());
        
        // Test with empty language code
        content = formatHandler.extractContent("", path);
        Assert.assertFalse(content.isEmpty());
    }
    
    /**
     * Test extraction of corrupted files.
     */
    @Test
    public void testCorruptedFileExtraction() throws Exception {
        // Create a corrupted image file
        Path corruptedImage = createCorruptedFile("corrupted.jpg");
        
        FormatHandler formatHandler = FormatHandlerUtil.find(MimeTypeUtil.guessMimeType(corruptedImage, "corrupted.jpg"));
        Assert.assertNotNull(formatHandler);
        
        try {
            String content = formatHandler.extractContent("eng", corruptedImage);
            // If extraction succeeds with empty content, that's acceptable
            Assert.assertEquals("", content.trim());
        } catch (Exception e) {
            // Expected behavior for corrupted files
            Assert.assertTrue(e.getMessage().contains("error") || 
                             e.getMessage().contains("corrupt") ||
                             e.getMessage().contains("invalid") ||
                             e.getMessage().contains("Failed"));
        }
    }
    
    /**
     * Helper method to create an empty file for testing.
     */
    private Path createEmptyFile(String filename) throws Exception {
        Path emptyFile = Paths.get(System.getProperty("java.io.tmpdir"), filename);
        java.nio.file.Files.write(emptyFile, new byte[0]);
        return emptyFile;
    }
    
    /**
     * Helper method to create a corrupted file for testing.
     */
    private Path createCorruptedFile(String filename) throws Exception {
        Path corruptedFile = Paths.get(System.getProperty("java.io.tmpdir"), filename);
        byte[] corruptData = {0x00, 0x01, 0x02, 0x03, 0x04}; // Not valid for any image format
        java.nio.file.Files.write(corruptedFile, corruptData);
        return corruptedFile;
    }
}