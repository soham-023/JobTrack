package com.jobtrack.repository;

import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT j FROM JobApplication j WHERE j.user.id = :userId " +
           "AND (:status IS NULL OR j.status = :status) " +
           "AND (:employmentType IS NULL OR j.employmentType = :employmentType) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(j.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(j.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<JobApplication> searchApplications(
            @Param("userId") Long userId,
            @Param("status") ApplicationStatus status,
            @Param("employmentType") EmploymentType employmentType,
            @Param("search") String search,
            Pageable pageable
    );

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ApplicationStatus status);

    @Query("SELECT j.status, COUNT(j) FROM JobApplication j WHERE j.user.id = :userId GROUP BY j.status")
    List<Object[]> countByStatusForUser(@Param("userId") Long userId);
}
