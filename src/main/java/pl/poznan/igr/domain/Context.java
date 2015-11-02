package pl.poznan.igr.domain;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.analysis.FDAnalysisSession;
import pl.poznan.igr.domain.analysis.Lme4ModelSession;
import pl.poznan.igr.domain.analysis.SufficientStatisticsSession;
import pl.poznan.igr.domain.type.Status;

@Entity
@Configurable
public class Context {

	@NotNull
	@Size(max = 64)
	private String username;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "MM")
	private Date started;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "MM")
	private Date finished;

	@Enumerated
	private Status status;

	@OneToOne(mappedBy = "context", cascade = CascadeType.ALL)
	private ImportSession importSession;

	@OneToOne(mappedBy = "context", cascade = CascadeType.ALL)
	private UnzipSession unzipSession;

	@OneToOne(mappedBy = "context", cascade = CascadeType.ALL)
	private FDAnalysisSession FDAnalysisSession;

    @OneToOne(mappedBy = "context", cascade = CascadeType.ALL)
    private SufficientStatisticsSession sufficientStatisticsSession;

    @OneToOne(mappedBy = "context", cascade = CascadeType.ALL)
    private Lme4ModelSession lme4ModelSession;

    @ManyToOne(cascade = CascadeType.ALL)
    private BlobFile resultFile;

	public Context(String username) {
		this.setStarted(new Date());
		this.setStatus(Status.NEW);
		this.setUsername(username);
		this.statusMessage = new Vector<>();
	}

	@Column
    @ElementCollection(targetClass=String.class)
	private List<String> statusMessage;

	public void addStatusMessage(String message) {
		
		statusMessage.add(message);
		System.err.println("Update of status message: " + statusMessage.listIterator());
	}

	public void setStatusMessage(String message) {
		addStatusMessage(message);
	}
	
	@SuppressWarnings("unused")
	private void setStatusMessage(List<String> sMessage) {
		this.statusMessage = sMessage;
	}
	
	public String getStatusMessage() {
		if (statusMessage != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : this.statusMessage) {
                sb.append(s);
                sb.append("\t\n");
            }
            return sb.toString();
        }
        return "";
	}


	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("username", "started", "finished", "status", "importSession", "unzipSession",
            "FDAnalysisSession", "sufficientStatisticsSession", "Lme4ModelSession", "zipSession", "statusMessage", "resultFile");

	public static final EntityManager entityManager() {
        EntityManager em = new Context().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countContexts() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Context o", Long.class).getSingleResult();
    }

	public static List<Context> findAllContexts() {
        return entityManager().createQuery("SELECT o FROM Context o", Context.class).getResultList();
    }

	public static List<Context> findAllContexts(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Context o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Context.class).getResultList();
    }

	public static Context findContext(Long id) {
        if (id == null) return null;
        return entityManager().find(Context.class, id);
    }

	public static List<Context> findContextEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Context o", Context.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<Context> findContextEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Context o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Context.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Context attached = Context.findContext(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public Context merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Context merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Context() {
        super();
    }

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	public boolean equals(Object obj) {
        if (!(obj instanceof Context)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Context rhs = (Context) obj;
        return new EqualsBuilder()
                .append(finished, rhs.finished)
                .append(id, rhs.id)
                .append(importSession, rhs.importSession)
                .append(username, rhs.username)
                .append(started, rhs.started)
                .append(FDAnalysisSession, rhs.FDAnalysisSession)
                .append(sufficientStatisticsSession, rhs.sufficientStatisticsSession)
                .append(lme4ModelSession, rhs.lme4ModelSession)
                .append(status, rhs.status)
                .append(unzipSession, rhs.unzipSession)
                .append(resultFile, rhs.resultFile)
                .isEquals();
    }

	public int hashCode() {
        return new HashCodeBuilder()
                .append(finished)
                .append(id)
                .append(importSession)
                .append(username)
                .append(started)
                .append(FDAnalysisSession)
                .append(sufficientStatisticsSession)
                .append(lme4ModelSession)
                .append(status)
                .append(unzipSession)
                .append(resultFile)
                .toHashCode();
    }

	public String getUsername() {
        return this.username;
    }

	public void setUsername(String username) {
        this.username = username;
    }

	public Date getStarted() {
        return this.started;
    }

	public void setStarted(Date started) {
        this.started = started;
    }

	public Date getFinished() {
        return this.finished;
    }

	public void setFinished(Date finished) {
        this.finished = finished;
    }

	public Status getStatus() {
        return this.status;
    }

	public void setStatus(Status status) {
        this.status = status;
    }

	public ImportSession getImportSession() {
        return this.importSession;
    }

	public void setImportSession(ImportSession importSession) {
        this.importSession = importSession;
    }

	public UnzipSession getUnzipSession() {
        return this.unzipSession;
    }

	public void setUnzipSession(UnzipSession unzipSession) {
        this.unzipSession = unzipSession;
    }

	public FDAnalysisSession getFDAnalysisSession() {
        return this.FDAnalysisSession;
    }

	public void setFDAnalysisSession(FDAnalysisSession fDAnalysisSession) {
        this.FDAnalysisSession = fDAnalysisSession;
    }

    public SufficientStatisticsSession getSufficientStatisticsSession() {
        return sufficientStatisticsSession;
    }

    public void setSufficientStatisticsSession(SufficientStatisticsSession sufficientStatisticsSession) {
        this.sufficientStatisticsSession = sufficientStatisticsSession;
    }

    public Lme4ModelSession getLme4ModelSession() {
        return lme4ModelSession;
    }

    public void setLme4ModelSession(Lme4ModelSession lme4ModelSession) {
        this.lme4ModelSession = lme4ModelSession;
    }

    public BlobFile getResultFile() {
        return this.resultFile;
    }

    public void setResultFile(BlobFile resultFile) {
        this.resultFile = resultFile;
    }
}
