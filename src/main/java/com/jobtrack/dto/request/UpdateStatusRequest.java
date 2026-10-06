package com.jobtrack.dto.request;

import com.jobtrack.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status cannot be null")
    private ApplicationStatus status;

    private String optionalNote;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(ApplicationStatus status) {
        this.status = status;
    }

    public UpdateStatusRequest(ApplicationStatus status, String optionalNote) {
        this.status = status;
        this.optionalNote = optionalNote;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getOptionalNote() {
        return optionalNote;
    }

    public void setOptionalNote(String optionalNote) {
        this.optionalNote = optionalNote;
    }
}
