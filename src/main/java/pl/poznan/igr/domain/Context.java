package pl.poznan.igr.domain;

import java.util.Date;

import javax.persistence.CascadeType;
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

	//TODO uporz¹dkowaæ labele przy wyswietlaniu weba - teraz 'List all Analyses' -> List all Contexts
	
    @NotNull
    @Size(max = 64)
    private String owner;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date started;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date finished;

    @Enumerated
    private Status status;
        
    @OneToOne(mappedBy = "context")
	private ImportSession importSession;
    
    @OneToOne(mappedBy = "context")
	private UnzipSession unzipSession;
    
    @OneToOne(mappedBy = "context")
	private StatsSession statsSession;
    
    public Context(String owner) {
    	this.setStarted(new Date());
    	this.setStatus(Status.NEW);
    	this.setOwner(owner);
	}
    
}
