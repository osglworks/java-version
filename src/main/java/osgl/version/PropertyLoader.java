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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * A helper class that load properties file from resource.
 */
class PropertyLoader {

    static PropertyLoader INSTANCE = new PropertyLoader();

    Properties loadFromResource(String packageName) {
        String versionPath = packageName.replace('.', '/') + "/.version";
        URL url = Version.class.getClassLoader().getResource(versionPath);
        return null == url ? null : loadFrom(url);
    }

    private Properties loadFrom(URL url) {
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            return null;
        }
        return properties;
    }

}
