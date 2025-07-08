package ru.red.lazaruscloud.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.red.lazaruscloud.annotation.impl.NotEmptyFileValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyFileValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyFile {
    String message() default "file is empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
