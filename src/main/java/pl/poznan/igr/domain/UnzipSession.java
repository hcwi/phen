package pl.poznan.igr.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class UnzipSession {

	private static final String UNZIP_SESSION_FOR_CONTEXT_QUERY = "SELECT z FROM UnzipSession z join z.context c "
			+ "WHERE c.id = :contextId";

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date creationDate;

	@NotNull
	@OneToOne
	private Context context;

	@NotNull
	private String unzipPath;

	public UnzipSession() {
		this.setCreationDate(new Date());
	}

	public static UnzipSession findUnzipSessionForContext(Context ctx) {
		checkNotNull(ctx);
		Query query = entityManager().createQuery(
				UNZIP_SESSION_FOR_CONTEXT_QUERY);
		query.setParameter("contextId", ctx.getId());
		return (UnzipSession) query.getSingleResult();
	}

	public String toString() {

		String s = this.getClass() + ": id " + this.getId() + " date "
				+ this.getCreationDate() + "path " + this.getUnzipPath();
		return s;
	}

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("UNZIP_SESSION_FOR_CONTEXT_QUERY", "creationDate", "context", "unzipPath");

	public static final EntityManager entityManager() {
        EntityManager em = new UnzipSession().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countUnzipSessions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM UnzipSession o", Long.class).getSingleResult();
    }

	public static List<UnzipSession> findAllUnzipSessions() {
        return entityManager().createQuery("SELECT o FROM UnzipSession o", UnzipSession.class).getResultList();
    }

	public static List<UnzipSession> findAllUnzipSessions(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UnzipSession o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UnzipSession.class).getResultList();
    }

	public static UnzipSession findUnzipSession(Long id) {
        if (id == null) return null;
        return entityManager().find(UnzipSession.class, id);
    }

	public static List<UnzipSession> findUnzipSessionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM UnzipSession o", UnzipSession.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<UnzipSession> findUnzipSessionEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UnzipSession o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UnzipSession.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            UnzipSession attached = UnzipSession.findUnzipSession(this.id);
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
    public UnzipSession merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        UnzipSession merged = this.entityManager.merge(this);
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

	public Date getCreationDate() {
        return this.creationDate;
    }

	public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

	public Context getContext() {
        return this.context;
    }

	public void setContext(Context context) {
        this.context = context;
    }

	public String getUnzipPath() {
        return this.unzipPath;
    }

	public void setUnzipPath(String unzipPath) {
        this.unzipPath = unzipPath;
    }
}