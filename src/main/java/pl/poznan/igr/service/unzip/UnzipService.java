package pl.poznan.igr.service.unzip;

import pl.poznan.igr.domain.Context;

public interface UnzipService {

	void unzipFile(Context context);
	
	String unzipFile(String path);

	void process(Context ctx);
}
