package com.jobtrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.dto.request.*;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import com.jobtrack.entity.enums.InterviewType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobTrackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should allow public access to root dashboard")
    void testRootEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should successfully register a new user and login")
    void testRegisterAndLogin() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "john.doe@example.com",
                "secretPassword123",
                "John Doe"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.user.fullName").value("John Doe"))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "secretPassword123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("invalid.user@example.com", "wrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject unauthenticated access to protected endpoints")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Should perform end-to-end workflow: Create application, update status, add interview, add note, and get analytics")
    void testCompleteApplicationLifecycle() throws Exception {
        // 1. Register a distinct user for isolated testing
        RegisterRequest registerReq = new RegisterRequest("jane.dev@example.com", "password321", "Jane Dev");
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode regNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String token = "Bearer " + regNode.get("token").asText();

        // 2. Create Job Application
        CreateJobApplicationRequest appReq = new CreateJobApplicationRequest();
        appReq.setCompanyName("Acme Corp");
        appReq.setJobTitle("Backend Software Engineer");
        appReq.setLocation("New York, NY");
        appReq.setEmploymentType(EmploymentType.FULL_TIME);
        appReq.setMinSalary(BigDecimal.valueOf(140000));
        appReq.setMaxSalary(BigDecimal.valueOf(160000));
        appReq.setStatus(ApplicationStatus.APPLIED);
        appReq.setAppliedDate(LocalDate.now());
        appReq.setInitialNotes("Found via company careers page.");

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Software Engineer"))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.notes.length()").value(1))
                .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long applicationId = createNode.get("id").asLong();

        // 3. Update Status via PATCH
        UpdateStatusRequest statusReq = new UpdateStatusRequest(ApplicationStatus.SCREENING, "Recruiter contacted via email");
        mockMvc.perform(patch("/api/v1/applications/" + applicationId + "/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCREENING"))
                .andExpect(jsonPath("$.notes.length()").value(2));

        // 4. Add Interview
        CreateInterviewRequest interviewReq = new CreateInterviewRequest();
        interviewReq.setRoundName("Round 1 - Technical Coding");
        interviewReq.setInterviewType(InterviewType.TECHNICAL);
        interviewReq.setScheduledAt(LocalDateTime.now().plusDays(3));
        interviewReq.setInterviewerName("Bob Tech");
        interviewReq.setInterviewerEmail("bob@acme.com");
        interviewReq.setLocationOrLink("https://zoom.us/j/987654321");

        mockMvc.perform(post("/api/v1/applications/" + applicationId + "/interviews")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(interviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roundName").value("Round 1 - Technical Coding"))
                .andExpect(jsonPath("$.interviewType").value("TECHNICAL"));

        // Application status should be automatically updated to INTERVIEWING
        mockMvc.perform(get("/api/v1/applications/" + applicationId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEWING"))
                .andExpect(jsonPath("$.interviews.length()").value(1));

        // 5. Add Note
        CreateNoteRequest noteReq = new CreateNoteRequest("Prep Notes", "Review Spring Security, Concurrency, and JPA caching.");
        mockMvc.perform(post("/api/v1/applications/" + applicationId + "/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Prep Notes"));

        // 6. Search and Filter Applications
        mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", token)
                        .param("search", "Acme")
                        .param("status", "INTERVIEWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].companyName").value("Acme Corp"));

        // 7. Get Analytics Stats
        mockMvc.perform(get("/api/v1/analytics/stats")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(1))
                .andExpect(jsonPath("$.activeApplications").value(1))
                .andExpect(jsonPath("$.totalInterviews").value(1))
                .andExpect(jsonPath("$.upcomingInterviewsCount").value(1))
                .andExpect(jsonPath("$.statusCounts.INTERVIEWING").value(1));
    }

    @Test
    @DisplayName("Should successfully upload, list, download, and delete a document attachment")
    void testDocumentAttachmentLifecycle() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("doc.user@example.com", "password123", "Doc User");
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = "Bearer " + objectMapper.readTree(regResult.getResponse().getContentAsString()).get("token").asText();

        // Create Application
        CreateJobApplicationRequest appReq = new CreateJobApplicationRequest();
        appReq.setCompanyName("GitHub");
        appReq.setJobTitle("Platform Engineer");
        MvcResult appResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long appId = objectMapper.readTree(appResult.getResponse().getContentAsString()).get("id").asLong();

        // 1. Upload Document (PDF)
        byte[] pdfContent = "%PDF-1.4 Mock resume content for testing".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Senior_Resume.pdf",
                "application/pdf",
                pdfContent
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/applications/" + appId + "/documents")
                        .file(file)
                        .param("documentType", "RESUME")
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFileName").value("Senior_Resume.pdf"))
                .andExpect(jsonPath("$.documentType").value("RESUME"))
                .andExpect(jsonPath("$.downloadUrl").isString())
                .andReturn();

        long docId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. List Documents for Application
        mockMvc.perform(get("/api/v1/applications/" + appId + "/documents")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(docId))
                .andExpect(jsonPath("$[0].originalFileName").value("Senior_Resume.pdf"));

        // 3. Download Document
        MvcResult downloadResult = mockMvc.perform(get("/api/v1/documents/" + docId + "/download")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Senior_Resume.pdf\""))
                .andReturn();

        assertThat(downloadResult.getResponse().getContentAsByteArray()).isEqualTo(pdfContent);

        // 4. Delete Document
        mockMvc.perform(delete("/api/v1/documents/" + docId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        // 5. Verify Document is gone
        mockMvc.perform(get("/api/v1/applications/" + appId + "/documents")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
