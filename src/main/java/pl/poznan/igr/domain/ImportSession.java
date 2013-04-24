package pl.poznan.igr.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;


@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
public class ImportSession {
	
	private static final String IMPORT_SESSION_FOR_CONTEXT_QUERY = "SELECT z FROM ImportSession z join z.context c "
			+ "WHERE c.id = :contextId";

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date creationDate;

	@NotNull
	@OneToOne
	private Context context;

	@ManyToOne(cascade=CascadeType.PERSIST)
	private BlobFile blobFile;
	
	public ImportSession(BlobFile blob) {
		this.setCreationDate(new Date());
		this.setBlobFile(blob);
	}
	
	public static ImportSession findImportSessionForContext(Context context) {
		checkNotNull(context);
		Query query = entityManager()
				.createQuery(IMPORT_SESSION_FOR_CONTEXT_QUERY);
		query.setParameter("contextId", context.getId());
		return (ImportSession) query.getSingleResult();
	}
}
