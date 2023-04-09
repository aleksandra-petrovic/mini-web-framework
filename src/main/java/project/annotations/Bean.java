package project.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Target(ElementType.TYPE)


public @interface Bean {
    String scope() default "singleton";
}
