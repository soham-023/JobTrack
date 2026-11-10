package com.jobtrack.controller;

import com.jobtrack.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobApplicationControllerTest {

    @Test
    void buildsPageableFromValidParameters() {
        Pageable pageable = JobApplicationController.buildPageable(2, 25, "companyName", "ASC");

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        assertThat(pageable.getSort().getOrderFor("companyName").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void defaultsMatchControllerDefaults() {
        Pageable pageable = JobApplicationController.buildPageable(0, 10, "createdAt", "desc");

        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> JobApplicationController.buildPageable(-1, 10, "createdAt", "desc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsPageSizeOutOfRange() {
        assertThatThrownBy(() -> JobApplicationController.buildPageable(0, 0, "createdAt", "desc"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> JobApplicationController.buildPageable(0, JobApplicationController.MAX_PAGE_SIZE + 1, "createdAt", "desc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsUnknownSortField() {
        assertThatThrownBy(() -> JobApplicationController.buildPageable(0, 10, "user.password", "desc"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("user.password");
    }

    @Test
    void rejectsUnknownSortDirection() {
        assertThatThrownBy(() -> JobApplicationController.buildPageable(0, 10, "createdAt", "sideways"))
                .isInstanceOf(BadRequestException.class);
    }
}
