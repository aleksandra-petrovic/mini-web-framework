package project.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Target(ElementType.FIELD)

public @interface Autowired {
    boolean verbose() default false;
}
