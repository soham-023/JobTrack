package com.jobtrack.service;

import com.jobtrack.entity.JobApplication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {

    private static final String CSV_HEADER = "Application ID,Company Name,Job Title,Status,Employment Type,Location,Min Salary,Max Salary,Currency,Applied Date,Deadline,Job URL,Interviews Count,Notes Count,Created At\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generateApplicationsCsv(List<JobApplication> applications) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Write UTF-8 BOM so Excel opens UTF-8 characters cleanly
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            writer.print(CSV_HEADER);

            for (JobApplication app : applications) {
                StringBuilder row = new StringBuilder();
                row.append(app.getId()).append(",");
                row.append(escapeCsv(app.getCompanyName())).append(",");
                row.append(escapeCsv(app.getJobTitle())).append(",");
                row.append(escapeCsv(app.getStatus() != null ? app.getStatus().name() : "")).append(",");
                row.append(escapeCsv(app.getEmploymentType() != null ? app.getEmploymentType().name() : "")).append(",");
                row.append(escapeCsv(app.getLocation())).append(",");
                row.append(app.getMinSalary() != null ? app.getMinSalary().toPlainString() : "").append(",");
                row.append(app.getMaxSalary() != null ? app.getMaxSalary().toPlainString() : "").append(",");
                row.append(escapeCsv(app.getSalaryCurrency())).append(",");
                row.append(app.getAppliedDate() != null ? app.getAppliedDate().format(DATE_FORMATTER) : "").append(",");
                row.append(app.getDeadline() != null ? app.getDeadline().format(DATE_FORMATTER) : "").append(",");
                row.append(escapeCsv(app.getJobUrl())).append(",");
                row.append(app.getInterviews() != null ? app.getInterviews().size() : 0).append(",");
                row.append(app.getNotes() != null ? app.getNotes().size() : 0).append(",");
                row.append(app.getCreatedAt() != null ? app.getCreatedAt().format(DATETIME_FORMATTER) : "");
                row.append("\n");

                writer.print(row.toString());
            }

            writer.flush();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate CSV export", ex);
        }

        return outputStream.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
