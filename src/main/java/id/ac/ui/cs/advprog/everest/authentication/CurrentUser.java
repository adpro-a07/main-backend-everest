package id.ac.ui.cs.advprog.everest.authentication;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {}
