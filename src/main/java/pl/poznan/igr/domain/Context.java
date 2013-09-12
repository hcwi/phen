package pl.poznan.igr.domain;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import pl.poznan.igr.domain.type.Status;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
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
		
		StringBuilder sb = new StringBuilder();
		for (String s : this.statusMessage) {
			sb.append(s);
			sb.append("\t\n");
		}
		return sb.toString();
	}

}
