package pl.poznan.igr.domain;

import java.util.Date;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import pl.poznan.igr.domain.type.Status;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Context {

    @NotNull
    @Size(max = 64)
    private String owner;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date started;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date finished;

    @ManyToOne
    private BlobFile uploadedFile;

    @Enumerated
    private Status status;
}
