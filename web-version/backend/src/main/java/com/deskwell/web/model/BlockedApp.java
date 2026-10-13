package com.deskwell.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blocked_apps")
public class BlockedApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "is_permanent")
    private Boolean isPermanent = false;

    @Column(name = "notes")
    private String notes;

    public BlockedApp() {}

    public BlockedApp(Application application, Boolean isPermanent) {
        this.application = application;
        this.isPermanent = isPermanent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public Boolean getIsPermanent() { return isPermanent; }
    public void setIsPermanent(Boolean isPermanent) { this.isPermanent = isPermanent; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
