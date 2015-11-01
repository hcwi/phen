package pl.poznan.igr.web.upload;

import org.springframework.web.multipart.MultipartFile;

public class UploadForm {

	private String owner;
	private MultipartFile fileContent;

	public String getOwner() {
        return this.owner;
    }

	public void setOwner(String owner) {
        this.owner = owner;
    }

	public MultipartFile getFileContent() {
        return this.fileContent;
    }

	public void setFileContent(MultipartFile fileContent) {
        this.fileContent = fileContent;
    }
}
