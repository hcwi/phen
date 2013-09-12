package pl.poznan.igr.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;

@RooJavaBean
@RooJpaActiveRecord
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
}