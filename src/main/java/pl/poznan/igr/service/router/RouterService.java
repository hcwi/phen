package pl.poznan.igr.service.router;

import pl.poznan.igr.domain.Context;

public interface RouterService {
	
	public void runNext(Context ctx);
}
