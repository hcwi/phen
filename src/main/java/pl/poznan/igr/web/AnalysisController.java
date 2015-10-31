package pl.poznan.igr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.service.stats.AnalysisAService;
import pl.poznan.igr.web.exception.ResourceNotFoundException;

@RequestMapping("/analysis/**")
@Controller
public class AnalysisController {

    @Autowired
    AnalysisAService analysisAService;

    @RequestMapping(method = RequestMethod.GET, value = "a/context/{contextId}")
    public ModelAndView analyzeAIfNotDone(@PathVariable Long contextId) {
        Context context = Context.findContext(contextId);
        if (context == null) {
            throw new ResourceNotFoundException();
        }

        analysisAService.analyze(context);

        return new ModelAndView("redirect:/contexts/" + contextId);
    }
}
