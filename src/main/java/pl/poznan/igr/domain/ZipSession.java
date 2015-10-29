package pl.poznan.igr.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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

@Entity
@Configurable
public class ZipSession {

	private static final String ZIP_SESSION_FOR_CONTEXT_QUERY = "SELECT z FROM ZipSession z join z.context c "
			+ "WHERE c.id = :contextId";

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date creationDate;

	@NotNull
	@OneToOne
	private Context context;

	@ManyToOne(cascade = CascadeType.ALL)
	private BlobFile blobFileEnriched;

	@ManyToOne(cascade = CascadeType.ALL)
	private BlobFile blobFileReduced;

	public ZipSession() {
		this.setCreationDate(new Date());
	}

	public static ZipSession findZipSessionForContext(Context ctx) {
		checkNotNull(ctx);
		Query query = entityManager()
				.createQuery(ZIP_SESSION_FOR_CONTEXT_QUERY);
		query.setParameter("contextId", ctx.getId());
		return (ZipSession) query.getSingleResult();
	}

	public String toString() {

		String s = this.getClass() + ": id " + this.getId() + " date "
				+ this.getCreationDate();
		return s;
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

	public BlobFile getBlobFileEnriched() {
        return this.blobFileEnriched;
    }

	public void setBlobFileEnriched(BlobFile blobFileEnriched) {
        this.blobFileEnriched = blobFileEnriched;
    }

	public BlobFile getBlobFileReduced() {
        return this.blobFileReduced;
    }

	public void setBlobFileReduced(BlobFile blobFileReduced) {
        this.blobFileReduced = blobFileReduced;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("ZIP_SESSION_FOR_CONTEXT_QUERY", "creationDate", "context", "blobFileEnriched", "blobFileReduced");

	public static final EntityManager entityManager() {
        EntityManager em = new ZipSession().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countZipSessions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ZipSession o", Long.class).getSingleResult();
    }

	public static List<ZipSession> findAllZipSessions() {
        return entityManager().createQuery("SELECT o FROM ZipSession o", ZipSession.class).getResultList();
    }

	public static List<ZipSession> findAllZipSessions(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ZipSession o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ZipSession.class).getResultList();
    }

	public static ZipSession findZipSession(Long id) {
        if (id == null) return null;
        return entityManager().find(ZipSession.class, id);
    }

	public static List<ZipSession> findZipSessionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ZipSession o", ZipSession.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<ZipSession> findZipSessionEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ZipSession o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ZipSession.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            ZipSession attached = ZipSession.findZipSession(this.id);
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
    public ZipSession merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ZipSession merged = this.entityManager.merge(this);
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
}