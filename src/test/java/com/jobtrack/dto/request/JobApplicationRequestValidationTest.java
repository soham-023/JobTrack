package com.jobtrack.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JobApplicationRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static CreateJobApplicationRequest validCreateRequest() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("Acme");
        request.setJobTitle("Backend Engineer");
        return request;
    }

    private static Set<String> invalidFields(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://jobs.example.com/123", "http://example.com", "HTTPS://Example.com/Path?q=1", "", "   "})
    void acceptsHttpLinksAndBlankJobUrl(String url) {
        CreateJobApplicationRequest request = validCreateRequest();
        request.setJobUrl(url);

        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"example.com", "ftp://example.com/file", "javascript:alert(1)", "https://exa mple.com"})
    void rejectsNonHttpJobUrl(String url) {
        CreateJobApplicationRequest request = validCreateRequest();
        request.setJobUrl(url);

        assertThat(invalidFields(validator.validate(request))).containsExactly("jobUrl");
    }

    @Test
    void updateRequestAppliesSameJobUrlRule() {
        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Acme");
        request.setJobTitle("Backend Engineer");
        request.setJobUrl("javascript:alert(1)");

        assertThat(invalidFields(validator.validate(request))).containsExactly("jobUrl");
    }

    @Test
    void appliedDateTodayOrEarlierIsValid() {
        CreateJobApplicationRequest request = validCreateRequest();
        request.setAppliedDate(LocalDate.now());
        assertThat(validator.validate(request)).isEmpty();

        request.setAppliedDate(LocalDate.now().minusMonths(2));
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void appliedDateInFutureIsRejected() {
        CreateJobApplicationRequest create = validCreateRequest();
        create.setAppliedDate(LocalDate.now().plusDays(3));
        assertThat(invalidFields(validator.validate(create))).containsExactly("appliedDate");

        UpdateJobApplicationRequest update = new UpdateJobApplicationRequest();
        update.setCompanyName("Acme");
        update.setJobTitle("Backend Engineer");
        update.setAppliedDate(LocalDate.now().plusDays(3));
        assertThat(invalidFields(validator.validate(update))).containsExactly("appliedDate");
    }
}
