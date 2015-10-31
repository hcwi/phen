package pl.poznan.igr.web;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.analysis.FDAnalysisSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.ZipSession;
import pl.poznan.igr.domain.type.Status;

@RequestMapping("/contexts")
@Controller
public class ContextController {
	
	@RequestMapping(value = "/{id}", produces = "text/html")
	public String show(@PathVariable("id") Long id, Model uiModel) {
		Context ctx = Context.findContext(id);

		addDateTimeFormatPatterns(uiModel);
		uiModel.addAttribute("context", ctx);
		uiModel.addAttribute("itemId", id);

		return "contexts/show";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Context context, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, context);
            return "contexts/create";
        }
        uiModel.asMap().clear();
        context.persist();
        return "redirect:/contexts/" + encodeUrlPathSegment(context.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new Context());
        return "contexts/create";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contexts", Context.findContextEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) Context.countContexts() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("contexts", Context.findAllContexts(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "contexts/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Context context = Context.findContext(id);
        context.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/contexts";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("context_started_date_format", DateTimeFormat.patternForStyle("MM", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("context_finished_date_format", DateTimeFormat.patternForStyle("MM", LocaleContextHolder.getLocale()));
    }

	void populateEditForm(Model uiModel, Context context) {
        uiModel.addAttribute("context", context);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("importsessions", ImportSession.findAllImportSessions());
        uiModel.addAttribute("statssessions", FDAnalysisSession.findAllStatsSessions());
        uiModel.addAttribute("unzipsessions", UnzipSession.findAllUnzipSessions());
        uiModel.addAttribute("zipsessions", ZipSession.findAllZipSessions());
        uiModel.addAttribute("statuses", Arrays.asList(Status.values()));
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
}
