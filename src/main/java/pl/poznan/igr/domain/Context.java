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
import pl.poznan.igr.domain.type.Status;

@Entity
@Configurable
public class Context {

	@NotNull
	@Size(max = 64)
	private String owner;

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
	private StatsSession statsSession;

	@OneToOne(mappedBy = "context", cascade = CascadeType.ALL)
	private ZipSession zipSession;

	public Context(String owner) {
		this.setStarted(new Date());
		this.setStatus(Status.NEW);
		this.setOwner(owner);
		this.statusMessage = new Vector<String>();
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

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("owner", "started", "finished", "status", "importSession", "unzipSession", "statsSession", "zipSession", "statusMessage");

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
        return new EqualsBuilder().append(finished, rhs.finished).append(id, rhs.id).append(importSession, rhs.importSession).append(owner, rhs.owner).append(started, rhs.started).append(statsSession, rhs.statsSession).append(status, rhs.status).append(unzipSession, rhs.unzipSession).append(zipSession, rhs.zipSession).isEquals();
    }

	public int hashCode() {
        return new HashCodeBuilder().append(finished).append(id).append(importSession).append(owner).append(started).append(statsSession).append(status).append(unzipSession).append(zipSession).toHashCode();
    }

	public String getOwner() {
        return this.owner;
    }

	public void setOwner(String owner) {
        this.owner = owner;
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

	public StatsSession getStatsSession() {
        return this.statsSession;
    }

	public void setStatsSession(StatsSession statsSession) {
        this.statsSession = statsSession;
    }

	public ZipSession getZipSession() {
        return this.zipSession;
    }

	public void setZipSession(ZipSession zipSession) {
        this.zipSession = zipSession;
    }
}
