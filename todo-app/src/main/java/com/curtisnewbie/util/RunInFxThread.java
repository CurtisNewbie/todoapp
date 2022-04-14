package com.curtisnewbie.util;

import java.lang.annotation.*;

/**
 * Annotation that tells a method will be executed in JavaFx's UI thread
 * <p>
 * It's only used for documentation.
 * </p>
 *
 * @author yongjie.zhuang
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface RunInFxThread {
}
