package pl.poznan.igr.service.stats;

public class RException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final String comment;

	public RException(String comment) {
		this.comment = comment;
	}

}
