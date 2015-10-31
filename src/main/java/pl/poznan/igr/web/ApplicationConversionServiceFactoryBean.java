package pl.poznan.igr.web;


import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.analysis.FDAnalysisSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.ZipSession;

@Configurable
/**
 * A central place to register application converters and formatters.
 */
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
				return new StringBuilder().append(is.getBlobFile().getFileName()).toString();
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
	

	public Converter<FDAnalysisSession, String> getStatsSessionToStringConverter() {
		return new Converter<FDAnalysisSession, String>() {
			public String convert(FDAnalysisSession ss) {
				return new StringBuilder().append(' ')
						.append(ss.getCreationDate()).toString();
			}
		};
	}
	
	public Converter<ZipSession, String> getZipSessionToStringConverter() {
		return new Converter<ZipSession, String>() {
			public String convert(ZipSession zs) {
				return new StringBuilder().append(zs.getBlobFileEnriched().getFileName()).append(' ').append(zs.getBlobFileReduced().getFileName())
						.append(zs.getCreationDate()).toString();
			}
		};
	}

	public Converter<Context, String> getContextToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<pl.poznan.igr.domain.Context, java.lang.String>() {
            public String convert(Context context) {
                return new StringBuilder().append(context.getOwner()).append(' ').append(context.getStarted()).append(' ').append(context.getFinished()).toString();
            }
        };
    }

	public Converter<Long, Context> getIdToContextConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, pl.poznan.igr.domain.Context>() {
            public pl.poznan.igr.domain.Context convert(java.lang.Long id) {
                return Context.findContext(id);
            }
        };
    }

	public Converter<String, Context> getStringToContextConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, pl.poznan.igr.domain.Context>() {
            public pl.poznan.igr.domain.Context convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Context.class);
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getContextToStringConverter());
        registry.addConverter(getIdToContextConverter());
        registry.addConverter(getStringToContextConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
