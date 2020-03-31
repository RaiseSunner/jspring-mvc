package top.raisesunner.springmvc.annotation;

import java.lang.annotation.*;

//表示该注解将会使用在类定义的位置
//注解在tomcat运行的过程可以通过反射获取注解的相关信息
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TopController {
    String value() default "";
}
