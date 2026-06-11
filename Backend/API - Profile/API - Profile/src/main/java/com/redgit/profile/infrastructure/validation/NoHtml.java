package com.redgit.profile.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {
    String message() default "Campo não pode conter HTML ou scripts";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}