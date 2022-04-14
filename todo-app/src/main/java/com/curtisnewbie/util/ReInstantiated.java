package com.curtisnewbie.util;

import java.lang.annotation.*;

/**
 * Annotation that tells a field may be re-instantiated overtime
 * <p>
 * It's only used for documentation.
 * </p>
 *
 * @author yongjie.zhuang
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ReInstantiated {

    String instantiatedBy() default "";

}
