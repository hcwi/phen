package pl.poznan.igr.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
public class BlobFile {

	private String fileName;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] content;

	private String contentType;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date created;

	public BlobFile() {
		this.setCreated(new Date());
	}

	public BlobFile(String name, byte[] content) {
		this.setCreated(new Date());
		this.setFileName(name);
		this.setContent(content);
	}

	public String toString() {

		String s = this.getClass() + ": id " + this.getId() + " date "
				+ this.getCreated() + " name " + this.getFileName() + " type "
				+ this.getContentType();
		return s;
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

	public String getFileName() {
        return this.fileName;
    }

	public void setFileName(String fileName) {
        this.fileName = fileName;
    }

	public byte[] getContent() {
        return this.content;
    }

	public void setContent(byte[] content) {
        this.content = content;
    }

	public String getContentType() {
        return this.contentType;
    }

	public void setContentType(String contentType) {
        this.contentType = contentType;
    }

	public Date getCreated() {
        return this.created;
    }

	public void setCreated(Date created) {
        this.created = created;
    }

	public boolean equals(Object obj) {
        if (!(obj instanceof BlobFile)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        BlobFile rhs = (BlobFile) obj;
        return new EqualsBuilder().append(contentType, rhs.contentType).append(created, rhs.created).append(fileName, rhs.fileName).append(id, rhs.id).isEquals();
    }

	public int hashCode() {
        return new HashCodeBuilder().append(contentType).append(created).append(fileName).append(id).toHashCode();
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("fileName", "content", "contentType", "created");

	public static final EntityManager entityManager() {
        EntityManager em = new BlobFile().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countBlobFiles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM BlobFile o", Long.class).getSingleResult();
    }

	public static List<BlobFile> findAllBlobFiles() {
        return entityManager().createQuery("SELECT o FROM BlobFile o", BlobFile.class).getResultList();
    }

	public static List<BlobFile> findAllBlobFiles(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM BlobFile o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, BlobFile.class).getResultList();
    }

	public static BlobFile findBlobFile(Long id) {
        if (id == null) return null;
        return entityManager().find(BlobFile.class, id);
    }

	public static List<BlobFile> findBlobFileEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM BlobFile o", BlobFile.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<BlobFile> findBlobFileEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM BlobFile o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, BlobFile.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            BlobFile attached = BlobFile.findBlobFile(this.id);
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
    public BlobFile merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        BlobFile merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
