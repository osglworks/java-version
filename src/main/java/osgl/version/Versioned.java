package osgl.version;

/*-
 * #%L
 * OSGL Version
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class of a library or app which has `.version` file presented.
 * 
 * Usually this annotation should be used to annotate on a typical class
 * that could be used to represent a library or application, e.g. the
 * main entry class of an app, or a facade class of a library.
 *
 * It is generally but not required that the class underline has a public
 * static final field of type `osgl.version.Version` and name `VERSION`,
 * for example:
 *
 * ```java
 * package org.mrcool.swissknife;
 *
 * .@Versioned
 * public class SwissKnife {
 *     // keep track the version of swissknife library
 *     public static final Version VERSION = Version.of(SwissKnife.class);
 * }
 * ```
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Versioned {
}