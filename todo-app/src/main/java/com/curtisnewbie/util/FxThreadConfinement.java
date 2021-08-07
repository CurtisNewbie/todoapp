package com.curtisnewbie.util;

import java.lang.annotation.*;

/**
 * Annotation that marks a class being confined in JavaFx's UI thread
 * <p>
 * It's only used for documentation. When such annotation presents, the use of the annotated type's objects must be
 * properly confined in the UI thread.
 * </p>
 *
 * @author yongjie.zhuang
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface FxThreadConfinement {
}
