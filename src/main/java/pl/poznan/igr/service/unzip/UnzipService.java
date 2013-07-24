package pl.poznan.igr.service.unzip;

import java.io.IOException;

import pl.poznan.igr.domain.Context;

public interface UnzipService {

	void process(Context ctx);

	void unzipFile(Context context);

	String unzipFile(String path) throws IOException;
}
