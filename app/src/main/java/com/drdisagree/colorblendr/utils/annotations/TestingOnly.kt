package com.drdisagree.colorblendr.utils.annotations

/*
 * Annotation to mark fields and methods used for testing purposes only.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class TestingOnly 
