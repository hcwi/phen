package pl.poznan.igr.web;


import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.StatsSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.ZipSession;

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
		registry.addConverter(getUnzipSessionToStringConverter());
		registry.addConverter(getStatsSessionToStringConverter());
		registry.addConverter(getZipSessionToStringConverter());
		}

	public Converter<ImportSession, String> getImportSessionToStringConverter() {
		return new Converter<ImportSession, String>() {
			public String convert(ImportSession is) {
				return new StringBuilder().append(is.getBlobFile().getFileName()).append(' ')
						.append(is.getCreationDate()).toString();
			}
		};
	}


	public Converter<UnzipSession, String> getUnzipSessionToStringConverter() {
		return new Converter<UnzipSession, String>() {
			public String convert(UnzipSession us) {
				return new StringBuilder()./*append(us.getUnzipPath()).*/append(' ')
						.append(us.getCreationDate()).toString();
			}
		};
	}
	

	public Converter<StatsSession, String> getStatsSessionToStringConverter() {
		return new Converter<StatsSession, String>() {
			public String convert(StatsSession ss) {
				return new StringBuilder().append(ss.getBlobFile().getFileName()).append(' ')
						.append(ss.getCreationDate()).toString();
			}
		};
	}
	
	public Converter<ZipSession, String> getZipSessionToStringConverter() {
		return new Converter<ZipSession, String>() {
			public String convert(ZipSession zs) {
				return new StringBuilder().append(zs.getBlobFile().getFileName()).append(' ')
						.append(zs.getCreationDate()).toString();
			}
		};
	}
}
