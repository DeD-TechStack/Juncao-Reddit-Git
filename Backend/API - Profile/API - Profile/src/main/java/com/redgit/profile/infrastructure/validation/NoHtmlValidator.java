package com.redgit.profile.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

    // Padrões para detectar HTML e scripts
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
            "<[^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "(javascript:|onerror=|onclick=|onload=|<script|</script>)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void initialize(NoHtml constraintAnnotation) {
        // Não precisa de inicialização
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null ou vazio é válido (use @NotBlank para obrigatoriedade)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Verificar se contém tags HTML
        if (HTML_TAG_PATTERN.matcher(value).find()) {
            return false;
        }

        // Verificar se contém scripts ou atributos de eventos
        if (SCRIPT_PATTERN.matcher(value).find()) {
            return false;
        }

        return true;
    }
}
