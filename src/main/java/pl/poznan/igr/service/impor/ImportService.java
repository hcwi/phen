package pl.poznan.igr.service.impor;

public interface ImportService {

	void importFile(String owner, String path);
	
	void importFile(String owner, String fileName, byte[] content);
}
