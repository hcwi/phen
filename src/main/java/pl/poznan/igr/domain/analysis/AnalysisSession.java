package pl.poznan.igr.domain.analysis;

import pl.poznan.igr.domain.Context;

import java.util.Date;

public interface AnalysisSession {

    Long getId();

    void setId(Long id);

    Integer getVersion();

    void setVersion(Integer version);

    Date getCreationDate();

    void setCreationDate(Date creationDate);

    Context getContext();

    void setContext(Context context);

    String getMessage();

    void setMessage(String message);

    AnalysisStatus getStatus();

    void setStatus(AnalysisStatus status);

    boolean isError();
}
