package top.raisesunner.springmvc.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TopAutowired {
    String value() default "";
}
