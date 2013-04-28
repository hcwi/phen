package pl.poznan.igr.web;


import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

import pl.poznan.igr.domain.ImportSession;

/**
 * A central place to register application converters and formatters.
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends
		FormattingConversionServiceFactoryBean {

	@SuppressWarnings("deprecation")
	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
		registry.addConverter(getImportSessionToStringConverter());


	}

	public Converter<ImportSession, String> getImportSessionToStringConverter() {
		return new Converter<ImportSession, String>() {
			public String convert(ImportSession is) {
				return new StringBuilder().append(is.getBlobFile().getFileName()).append(' ')
						.append(is.getCreationDate()).toString();
			}
		};
	}


}
