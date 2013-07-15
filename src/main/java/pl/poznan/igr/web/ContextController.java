package pl.poznan.igr.web;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import pl.poznan.igr.domain.Context;

@RequestMapping("/contexts")
@Controller
@RooWebScaffold(path = "contexts", formBackingObject = Context.class, update = false, delete = false, create = false)
public class ContextController {
	
	@RequestMapping(value = "/{id}", produces = "text/html")
	public String show(@PathVariable("id") Long id, Model uiModel) {
		Context ctx = Context.findContext(id);

		addDateTimeFormatPatterns(uiModel);
		uiModel.addAttribute("context", ctx);
		uiModel.addAttribute("itemId", id);

		return "contexts/show";
	}
}
