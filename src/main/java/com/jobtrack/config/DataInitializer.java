package com.jobtrack.config;

import com.jobtrack.entity.Interview;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.Note;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.entity.enums.InterviewType;
import com.jobtrack.entity.enums.Role;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.JobApplicationRepository;
import com.jobtrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.jobtrack.service.InterviewReminderService interviewReminderService;

    public DataInitializer(UserRepository userRepository,
                           JobApplicationRepository jobApplicationRepository,
                           InterviewRepository interviewRepository,
                           PasswordEncoder passwordEncoder,
                           com.jobtrack.service.InterviewReminderService interviewReminderService) {
        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewRepository = interviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.interviewReminderService = interviewReminderService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Seeding initial demo data for JobTrack...");

        User demoUser = new User();
        demoUser.setEmail("demo@jobtrack.com");
        demoUser.setPassword(passwordEncoder.encode("password123"));
        demoUser.setFullName("Demo Engineer");
        demoUser.setRole(Role.ROLE_USER);
        demoUser = userRepository.save(demoUser);

        // App 1: Google
        JobApplication app1 = new JobApplication();
        app1.setUser(demoUser);
        app1.setCompanyName("Google");
        app1.setJobTitle("Senior Backend Engineer");
        app1.setJobUrl("https://careers.google.com/jobs/results/12345");
        app1.setLocation("Mountain View, CA (Hybrid)");
        app1.setEmploymentType(EmploymentType.FULL_TIME);
        app1.setMinSalary(BigDecimal.valueOf(180000));
        app1.setMaxSalary(BigDecimal.valueOf(230000));
        app1.setSalaryCurrency("USD");
        app1.setStatus(ApplicationStatus.INTERVIEWING);
        app1.setAppliedDate(LocalDate.now().minusDays(14));
        app1.setDeadline(LocalDate.now().plusDays(20));

        Note note1 = new Note(app1, "Recruiter Call Summary", "Passed phone screen. Focus next on distributed systems and concurrency.");
        app1.addNote(note1);
        jobApplicationRepository.save(app1);

        Interview interview1 = new Interview();
        interview1.setJobApplication(app1);
        interview1.setRoundName("System Design Round");
        interview1.setInterviewType(InterviewType.SYSTEM_DESIGN);
        interview1.setScheduledAt(LocalDateTime.now().plusHours(18));
        interview1.setInterviewerName("Sarah Connor");
        interview1.setInterviewerEmail("sconnor@google.com");
        interview1.setLocationOrLink("https://meet.google.com/abc-defg-hij");
        interview1.setStatus(InterviewStatus.SCHEDULED);
        interviewRepository.save(interview1);

        // App 2: Netflix
        JobApplication app2 = new JobApplication();
        app2.setUser(demoUser);
        app2.setCompanyName("Netflix");
        app2.setJobTitle("Java Platform Engineer");
        app2.setJobUrl("https://jobs.netflix.com/jobs/67890");
        app2.setLocation("Remote");
        app2.setEmploymentType(EmploymentType.FULL_TIME);
        app2.setMinSalary(BigDecimal.valueOf(210000));
        app2.setMaxSalary(BigDecimal.valueOf(250000));
        app2.setSalaryCurrency("USD");
        app2.setStatus(ApplicationStatus.OFFER);
        app2.setAppliedDate(LocalDate.now().minusDays(30));
        app2.addNote(new Note(app2, "Offer Received", "Formal offer letter received with competitive equity grant. Reviewing benefits."));
        jobApplicationRepository.save(app2);

        // App 3: Stripe
        JobApplication app3 = new JobApplication();
        app3.setUser(demoUser);
        app3.setCompanyName("Stripe");
        app3.setJobTitle("Infrastructure Software Engineer");
        app3.setJobUrl("https://stripe.com/jobs/infra");
        app3.setLocation("San Francisco, CA");
        app3.setEmploymentType(EmploymentType.FULL_TIME);
        app3.setMinSalary(BigDecimal.valueOf(175000));
        app3.setMaxSalary(BigDecimal.valueOf(215000));
        app3.setSalaryCurrency("USD");
        app3.setStatus(ApplicationStatus.APPLIED);
        app3.setAppliedDate(LocalDate.now().minusDays(3));
        app3.addNote(new Note(app3, "Application Submitted", "Referred by campus alumni. Awaiting confirmation."));
        jobApplicationRepository.save(app3);

        // App 4: Amazon
        JobApplication app4 = new JobApplication();
        app4.setUser(demoUser);
        app4.setCompanyName("Amazon");
        app4.setJobTitle("Software Development Engineer II");
        app4.setJobUrl("https://amazon.jobs/en/jobs/998877");
        app4.setLocation("Seattle, WA");
        app4.setEmploymentType(EmploymentType.FULL_TIME);
        app4.setMinSalary(BigDecimal.valueOf(165000));
        app4.setMaxSalary(BigDecimal.valueOf(195000));
        app4.setSalaryCurrency("USD");
        app4.setStatus(ApplicationStatus.SCREENING);
        app4.setAppliedDate(LocalDate.now().minusDays(7));
        jobApplicationRepository.save(app4);

        Interview interview2 = new Interview();
        interview2.setJobApplication(app4);
        interview2.setRoundName("Technical Phone Screen");
        interview2.setInterviewType(InterviewType.TECHNICAL);
        interview2.setScheduledAt(LocalDateTime.now().plusDays(4).withHour(11).withMinute(30));
        interview2.setInterviewerName("Alex Mercer");
        interview2.setInterviewerEmail("amercer@amazon.com");
        interview2.setLocationOrLink("https://chime.aws/12345678");
        interview2.setStatus(InterviewStatus.SCHEDULED);
        interviewRepository.save(interview2);

        // Run initial reminder check on seeded data
        interviewReminderService.checkAndSendReminders();

        log.info("Demo data seeding completed. Demo credentials: demo@jobtrack.com / password123");
    }
}
