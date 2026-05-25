package org.gmautostop.hitchlogmp.domain

/**
 * Annotation to specify Excel column name for XLSX export.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcelColumn(val name: String, val ignore: Boolean = false)
