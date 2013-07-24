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
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
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
	private BlobFile blobFile;

	public ZipSession() {
		this.setCreationDate(new Date());
	}

	public static ZipSession findZipSessionForContext(Context ctx) {
		checkNotNull(ctx);
		Query query = entityManager().createQuery(
				ZIP_SESSION_FOR_CONTEXT_QUERY);
		query.setParameter("contextId", ctx.getId());
		return (ZipSession) query.getSingleResult();
	}
}