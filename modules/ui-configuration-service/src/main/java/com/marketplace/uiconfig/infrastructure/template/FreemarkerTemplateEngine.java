package com.marketplace.uiconfig.infrastructure.template;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * FreeMarker template engine for rendering dynamic templates.
 * Used for both UI configurations and notification templates.
 * 
 * Follows Clean Code and Object Calisthenics principles.
 */
@Component
public class FreemarkerTemplateEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerTemplateEngine.class);
    
    private final Configuration configuration;
    private final StringTemplateLoader templateLoader;
    
    public FreemarkerTemplateEngine() {
        this.configuration = createConfiguration();
        this.templateLoader = new StringTemplateLoader();
        this.configuration.setTemplateLoader(templateLoader);
        
        logger.info("FreeMarker template engine initialized");
    }
    
    /**
     * Renders a template with given data model.
     * 
     * @param templateName unique template identifier
     * @param templateContent FreeMarker template content
     * @param dataModel data to be merged with template
     * @return rendered string
     * @throws TemplateRenderingException if rendering fails
     */
    public String render(String templateName, String templateContent, Map<String, Object> dataModel) {
        validateInputs(templateName, templateContent, dataModel);
        
        try {
            registerTemplate(templateName, templateContent);
            Template template = loadTemplate(templateName);
            return processTemplate(template, dataModel);
        } catch (IOException | TemplateException exception) {
            logRenderingError(templateName, exception);
            throw new TemplateRenderingException(
                "Failed to render template: " + templateName,
                exception
            );
        }
    }
    
    /**
     * Renders a template with given data model and locale.
     * 
     * @param templateName unique template identifier
     * @param templateContent FreeMarker template content
     * @param dataModel data to be merged with template
     * @param locale locale for template rendering
     * @return rendered string
     * @throws TemplateRenderingException if rendering fails
     */
    public String render(
        String templateName,
        String templateContent,
        Map<String, Object> dataModel,
        Locale locale
    ) {
        validateInputs(templateName, templateContent, dataModel);
        validateLocale(locale);
        
        try {
            registerTemplate(templateName, templateContent);
            Template template = loadTemplateWithLocale(templateName, locale);
            return processTemplate(template, dataModel);
        } catch (IOException | TemplateException exception) {
            logRenderingError(templateName, exception);
            throw new TemplateRenderingException(
                "Failed to render template: " + templateName,
                exception
            );
        }
    }
    
    /**
     * Validates template syntax without rendering.
     * 
     * @param templateName unique template identifier
     * @param templateContent FreeMarker template content
     * @return validation result
     */
    public TemplateValidationResult validate(String templateName, String templateContent) {
        validateTemplateName(templateName);
        validateTemplateContent(templateContent);
        
        try {
            registerTemplate(templateName, templateContent);
            loadTemplate(templateName);
            return TemplateValidationResult.valid();
        } catch (IOException exception) {
            return TemplateValidationResult.invalid("Template syntax error: " + exception.getMessage());
        }
    }
    
    private Configuration createConfiguration() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_32);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);
        config.setFallbackOnNullLoopVariable(false);
        return config;
    }
    
    private void registerTemplate(String templateName, String templateContent) {
        templateLoader.putTemplate(templateName, templateContent);
    }
    
    private Template loadTemplate(String templateName) throws IOException {
        return configuration.getTemplate(templateName);
    }
    
    private Template loadTemplateWithLocale(String templateName, Locale locale) throws IOException {
        return configuration.getTemplate(templateName, locale);
    }
    
    private String processTemplate(Template template, Map<String, Object> dataModel) 
        throws IOException, TemplateException {
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        return writer.toString();
    }
    
    private void validateInputs(String templateName, String templateContent, Map<String, Object> dataModel) {
        validateTemplateName(templateName);
        validateTemplateContent(templateContent);
        validateDataModel(dataModel);
    }
    
    private void validateTemplateName(String templateName) {
        if (isBlank(templateName)) {
            throw new IllegalArgumentException("Template name cannot be blank");
        }
    }
    
    private void validateTemplateContent(String templateContent) {
        if (isBlank(templateContent)) {
            throw new IllegalArgumentException("Template content cannot be blank");
        }
    }
    
    private void validateDataModel(Map<String, Object> dataModel) {
        if (dataModel == null) {
            throw new IllegalArgumentException("Data model cannot be null");
        }
    }
    
    private void validateLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private void logRenderingError(String templateName, Exception exception) {
        logger.error(
            "Failed to render template: {}. Error: {}",
            templateName,
            exception.getMessage(),
            exception
        );
    }
    
    /**
     * Exception thrown when template rendering fails.
     */
    public static class TemplateRenderingException extends RuntimeException {
        public TemplateRenderingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Result of template validation.
     */
    public static final class TemplateValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private TemplateValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static TemplateValidationResult valid() {
            return new TemplateValidationResult(true, null);
        }
        
        public static TemplateValidationResult invalid(String errorMessage) {
            return new TemplateValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String errorMessage() {
            return errorMessage;
        }
    }
}
