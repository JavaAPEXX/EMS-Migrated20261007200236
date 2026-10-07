package com.employee.system.service;

import com.employee.system.dto.DocumentDTO;
import com.employee.system.entity.Document;
import com.employee.system.entity.Employee;
import com.employee.system.repository.DocumentRepository;
import com.employee.system.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    /**
     * Replace the private final {@code uploadsDir} field with a temporary directory.
     */
    @BeforeEach
    void setUp() throws Exception {
        Field uploadsDirField = DocumentService.class.getDeclaredField("uploadsDir");
        uploadsDirField.setAccessible(true);
        uploadsDirField.set(documentService, tempDir);
        // Ensure init() creates the directory (it will be the tempDir itself)
        documentService.init();
    }

    @Test
    @DisplayName("Given valid employee and file when uploadDocument then returns DTO with stored file")
    void givenValidEmployeeAndFile_whenUploadDocument_thenReturnsDTOWithStoredFile() throws IOException {
        // Arrange
        Long employeeId = 1L;
        String docType = "contract";
        Employee employee = new Employee();
        employee.setId(employeeId);

        MultipartFile multipartFile = mock(MultipartFile.class);
        String originalName = "resume.pdf";
        byte[] content = "dummy content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        when(multipartFile.getOriginalFilename()).thenReturn(originalName);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        Document savedDocument = new Document();
        savedDocument.setId(100L);
        savedDocument.setEmployee(employee);
        savedDocument.setDocType(docType);
        savedDocument.setOriginalFileName(originalName);
        savedDocument.setStoredFileName("placeholder"); // will be overwritten by service
        savedDocument.setContentType("application/pdf");
        savedDocument.setSize((long) content.length);
        when(documentRepository.save(docCaptor.capture())).thenReturn(savedDocument);

        // Act
        DocumentDTO result = documentService.uploadDocument(multipartFile, employeeId, docType);

        // Assert
        Document captured = docCaptor.getValue();
        assertEquals(employee, captured.getEmployee(), "Employee should be set on Document");
        assertEquals(docType, captured.getDocType(), "Doc type should be set");
        assertEquals(originalName, captured.getOriginalFileName(), "Original filename should be preserved");
        assertNotNull(captured.getStoredFileName(), "Stored filename should be generated");
        assertTrue(captured.getStoredFileName().contains("_" + originalName), "Stored filename should contain original name");
        assertEquals("application/pdf", captured.getContentType(), "Content type should be set");
        assertEquals((Long) (long) content.length, captured.getSize(), "Size should be set");

        // Verify file was physically copied
        Path expectedFile = tempDir.resolve(captured.getStoredFileName());
        assertTrue(Files.exists(expectedFile), "Uploaded file should exist on disk");
        assertArrayEquals(content, Files.readAllBytes(expectedFile), "File content should match uploaded data");

        // Verify DTO mapping
        assertEquals(savedDocument.getId(), result.getId());
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(docType, result.getDocType());
        assertEquals(originalName, result.getOriginalFileName());
        assertEquals(captured.getStoredFileName(), result.getStoredFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals((Long) (long) content.length, result.getSize());

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    @DisplayName("Given null original filename when uploadDocument then uses default unnamed filename")
    void givenNullOriginalFilename_whenUploadDocument_thenUsesUnnamedFilename() throws IOException {
        // Arrange
        Long employeeId = 2L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getContentType()).thenReturn("application/octet-stream");
        when(multipartFile.getSize()).thenReturn(0L);

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document d = invocation.getArgument(0);
            d.setId(200L);
            return d;
        });

        // Act
        DocumentDTO dto = documentService.uploadDocument(multipartFile, employeeId, "misc");

        // Assert
        assertEquals("unnamed", dto.getOriginalFileName(), "Original filename should be replaced with 'unnamed'");
        assertTrue(dto.getStoredFileName().contains("unnamed"), "Stored filename should contain 'unnamed'");
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    @DisplayName("Given non‑existing employee when uploadDocument then throws RuntimeException")
    void givenNonExistingEmployee_whenUploadDocument_thenThrowsRuntimeException() {
        // Arrange
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.uploadDocument(multipartFile, employeeId, "type"));
        assertTrue(ex.getMessage().contains("Employee not found"), "Exception message should indicate missing employee");
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given existing document and present file when downloadDocument then returns UrlResource")
    void givenExistingDocumentAndPresentFile_whenDownloadDocument_thenReturnsUrlResource() throws IOException {
        // Arrange
        Long docId = 10L;
        Document document = new Document();
        document.setId(docId);
        document.setStoredFileName("stored_file.txt");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        // create the physical file
        Path filePath = tempDir.resolve(document.getStoredFileName());
        Files.writeString(filePath, "content");

        // Act
        Resource resource = documentService.downloadDocument(docId);

        // Assert
        assertTrue(resource instanceof UrlResource, "Resource should be a UrlResource");
        assertEquals(filePath.toUri(), resource.getURI(), "Resource URI should point to stored file");
        assertTrue(resource.exists(), "Resource should exist");
        verify(documentRepository, times(1)).findById(docId);
    }

    @Test
    @DisplayName("Given non‑existing document when downloadDocument then throws RuntimeException")
    void givenNonExistingDocument_whenDownloadDocument_thenThrowsRuntimeException() {
        // Arrange
        Long docId = 555L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.downloadDocument(docId));
        assertTrue(ex.getMessage().contains("Document not found"), "Exception should indicate missing document");
        verify(documentRepository, times(1)).findById(docId);
    }

    @Test
    @DisplayName("Given existing document but missing file when downloadDocument then throws RuntimeException")
    void givenExistingDocumentButMissingFile_whenDownloadDocument_thenThrowsRuntimeException() {
        // Arrange
        Long docId = 20L;
        Document document = new Document();
        document.setId(docId);
        document.setStoredFileName("nonexistent.txt");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        // Ensure file does NOT exist
        Path missing = tempDir.resolve(document.getStoredFileName());
        assertFalse(Files.exists(missing));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.downloadDocument(docId));
        assertTrue(ex.getMessage().contains("Stored file missing"), "Exception should indicate missing stored file");
    }

    @Test
    @DisplayName("Given valid employee with documents when listByEmployee then returns DTO list")
    void givenValidEmployeeWithDocuments_whenListByEmployee_thenReturnsDTOList() {
        // Arrange
        Long employeeId = 3L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setEmployee(employee);
        doc1.setDocType("type1");
        doc1.setOriginalFileName("orig1");
        doc1.setStoredFileName("stored1");
        doc1.setContentType("type/a");
        doc1.setSize(10L);

        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setEmployee(employee);
        doc2.setDocType("type2");
        doc2.setOriginalFileName("orig2");
        doc2.setStoredFileName("stored2");
        doc2.setContentType("type/b");
        doc2.setSize(20L);

        when(documentRepository.findByEmployee(employee)).thenReturn(List.of(doc1, doc2));

        // Act
        List<DocumentDTO> result = documentService.listByEmployee(employeeId);

        // Assert
        assertEquals(2, result.size(), "Should return two DTOs");
        Set<Long> ids = Set.of(result.get(0).getId(), result.get(1).getId());
        assertTrue(ids.containsAll(Set.of(1L, 2L)), "DTO IDs should match persisted documents");
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(documentRepository, times(1)).findByEmployee(employee);
    }

    @Test
    @DisplayName("Given non‑existing employee when listByEmployee then throws RuntimeException")
    void givenNonExistingEmployee_whenListByEmployee_thenThrowsRuntimeException() {
        // Arrange
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.listByEmployee(employeeId));
        assertTrue(ex.getMessage().contains("Employee not found"));
        verify(documentRepository, never()).findByEmployee(any());
    }

    @Test
    @DisplayName("Given existing document with file when deleteDocument then removes file and repository entry")
    void givenExistingDocumentWithFile_whenDeleteDocument_thenRemovesFileAndDeletesEntity() throws IOException {
        // Arrange
        Long docId = 30L;
        Document document = new Document();
        document.setId(docId);
        document.setStoredFileName("to_delete.txt");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        // create the file that should be deleted
        Path filePath = tempDir.resolve(document.getStoredFileName());
        Files.writeString(filePath, "to be deleted");
        assertTrue(Files.exists(filePath), "File must exist before deletion");

        // Act
        documentService.deleteDocument(docId);

        // Assert
        assertFalse(Files.exists(filePath), "File should be deleted from filesystem");
        verify(documentRepository, times(1)).delete(document);
    }

    @Test
    @DisplayName("Given non‑existing document when deleteDocument then throws RuntimeException")
    void givenNonExistingDocument_whenDeleteDocument_thenThrowsRuntimeException() {
        // Arrange
        Long docId = 777L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.deleteDocument(docId));
        assertTrue(ex.getMessage().contains("Document not found"));
        verify(documentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Given init method when called then creates uploads directory")
    void givenInitMethod_whenCalled_thenCreatesUploadsDirectory() throws IOException {
        // Arrange
        // uploadsDir already points to tempDir; delete it to simulate missing directory
        Files.deleteIfExists(tempDir);
        assertFalse(Files.exists(tempDir), "Temp directory should be removed before init");

        // Act
        documentService.init();

        // Assert
        assertTrue(Files.isDirectory(tempDir), "init should recreate the uploads directory");
    }
}