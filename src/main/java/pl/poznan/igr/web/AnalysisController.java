package pl.poznan.igr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.service.stats.Lme4ModelService;
import pl.poznan.igr.service.stats.FDAnalysisService;
import pl.poznan.igr.service.stats.SufficientStatisticsService;
import pl.poznan.igr.web.exception.ResourceNotFoundException;

import java.io.IOException;

@RequestMapping("/analysis/**")
@Controller
public class AnalysisController {

    private

    @Autowired
    FDAnalysisService fDAnalysisService;

    @Autowired
    SufficientStatisticsService sufficientStatisticsService;

    @Autowired
    Lme4ModelService lme4ModelService;

    @RequestMapping(method = RequestMethod.GET, value = "time_series_analysis/context/{contextId}")
    public ModelAndView analyzeFDAnalysis(@PathVariable Long contextId) throws IOException {
        Context context = Context.findContext(contextId);
        if (context == null) {
            throw new ResourceNotFoundException();
        }

        fDAnalysisService.analyze(context);

        return new ModelAndView("redirect:/contexts/" + contextId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "sufficient_statistics/context/{contextId}")
    public ModelAndView analyzeSufficientStatistics(@PathVariable Long contextId) throws IOException {
        Context context = Context.findContext(contextId);
        if (context == null) {
            throw new ResourceNotFoundException();
        }

        sufficientStatisticsService.analyze(context);

        return new ModelAndView("redirect:/contexts/" + contextId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "estimation_of_effects/context/{contextId}")
    public ModelAndView analyzeLme4Model(@PathVariable Long contextId) throws IOException {
        Context context = Context.findContext(contextId);
        if (context == null) {
            throw new ResourceNotFoundException();
        }

        lme4ModelService.analyze(context);

        return new ModelAndView("redirect:/contexts/" + contextId);
    }
}
