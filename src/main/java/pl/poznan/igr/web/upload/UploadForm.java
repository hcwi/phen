package pl.poznan.igr.web.upload;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.web.multipart.MultipartFile;

@RooJavaBean
public class UploadForm {

	private String owner;
	private MultipartFile fileContent;
}
