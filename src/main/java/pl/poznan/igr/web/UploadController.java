package pl.poznan.igr.web;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import pl.poznan.igr.service.impor.ImportService;
import pl.poznan.igr.web.upload.UploadForm;

@RequestMapping("/upload/**")
@Controller
public class UploadController {

	private final static Logger log = LoggerFactory
			.getLogger(UploadController.class);

	@Autowired
	private ImportService importService;

	@RequestMapping(value = "file", method = RequestMethod.POST)
	public ModelAndView putNewFile(@ModelAttribute("form") UploadForm form) {
		MultipartFile multipartFile = form.getFileContent();
		try {
			importService.importFile(form.getOwner(),
					multipartFile.getOriginalFilename(), multipartFile.getBytes());
			return new ModelAndView("redirect:/contexts");
		} catch (IOException e) {
			log.error("Can't upload", e);
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value = "file", method = RequestMethod.GET)
	public ModelAndView getNewFileForm() {
		ModelAndView mav = new ModelAndView("upload/file");
		mav.addObject("form", new UploadForm());
		return mav;
	}
}
