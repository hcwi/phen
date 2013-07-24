package pl.poznan.igr.service.zip;

import pl.poznan.igr.domain.Context;

public interface ZipService {

	void process(Context ctx);

	String rezipFiles(String path);
}
