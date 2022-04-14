package com.curtisnewbie.util;

import java.lang.annotation.*;

/**
 * Annotation that marks a field should be synchronized by the specified variable
 * <p>
 * It's only used for documentation.
 * </p>
 *
 * @author yongjie.zhuang
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface LockedBy {

    /** field name */
    String field();
}
