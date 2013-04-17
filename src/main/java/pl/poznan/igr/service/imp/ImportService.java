package pl.poznan.igr.service.imp;

import org.springframework.web.multipart.MultipartFile;

public interface ImportService {

	void importFile(String owner, String path);
	
	void importFile(String owner, MultipartFile file);
}
