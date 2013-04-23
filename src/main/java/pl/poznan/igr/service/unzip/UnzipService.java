package pl.poznan.igr.service.unzip;

import pl.poznan.igr.domain.Context;

public interface UnzipService {

	void unzipFile(Context context);
	
	void unzipFile(String path);
}
