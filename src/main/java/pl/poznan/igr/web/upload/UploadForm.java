package pl.poznan.igr.web.upload;

import org.springframework.web.multipart.MultipartFile;

public class UploadForm {

	private String username;
	private MultipartFile fileContent;

	public String getUsername() {
        return this.username;
    }

	public void setUsername(String username) {
        this.username = username;
    }

	public MultipartFile getFileContent() {
        return this.fileContent;
    }

	public void setFileContent(MultipartFile fileContent) {
        this.fileContent = fileContent;
    }
}
