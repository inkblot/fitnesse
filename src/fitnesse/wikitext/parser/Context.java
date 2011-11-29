package fitnesse.wikitext.parser;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/28/11
 * Time: 9:02 PM
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
@BindingAnnotation
public @interface Context {
}
