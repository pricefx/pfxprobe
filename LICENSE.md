# License

## pfxprobe License

Copyright (c) 2025 Pricefx

pfxprobe is licensed under the MIT License.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

## Third-Party Dependencies

This software includes third-party open source components.
Each component has its own license and copyright notice.
The following is a list of all third-party dependencies included in this project.

### Runtime Dependencies

**Apache License 2.0:**
- Apache Ant Core (org.apache.ant:ant:1.10.11)
- Apache Ant + ANTLR (org.apache.ant:ant-antlr:1.10.11)
- Apache Ant + JUnit (org.apache.ant:ant-junit:1.10.11)
- Apache Ant Launcher (org.apache.ant:ant-launcher:1.10.11)
- Apache Commons CLI (commons-cli:commons-cli:1.4)
- Apache Groovy (org.codehaus.groovy:groovy:3.0.17)
- Apache Groovy (org.codehaus.groovy:groovy-ant:3.0.9)
- Apache Groovy (org.codehaus.groovy:groovy-docgenerator:3.0.9)
- Apache Groovy (org.codehaus.groovy:groovy-groovydoc:3.0.9)
- Apache Groovy (org.codehaus.groovy:groovy-json:3.0.17)
- Apache Groovy (org.codehaus.groovy:groovy-templates:3.0.9)
- Apache Groovy (org.codehaus.groovy:groovy-xml:3.0.9)
- cglib (cglib:cglib:3.3.0)
- CodeNarc (org.codenarc:CodeNarc:3.2.0)
- GMetrics (org.gmetrics:GMetrics:2.1.0)
- javaparser-core (com.github.javaparser:javaparser-core:3.23.0) - *Used under Apache 2.0 terms*
- org.apiguardian:apiguardian-api (org.apiguardian:apiguardian-api:1.1.2)
- org.opentest4j:opentest4j (org.opentest4j:opentest4j:1.2.0)
- QDox (com.thoughtworks.qdox:qdox:1.12.1)

**BSD License:**
- asm (org.ow2.asm:asm:7.1)
- Hamcrest (org.hamcrest:hamcrest:2.2)
- Hamcrest Core (org.hamcrest:hamcrest-core:1.3)

**MIT License:**
- SLF4J API Module (org.slf4j:slf4j-api:1.7.35)

### Test-Only Dependencies

**Apache License 2.0:**
- Spock Framework - Core Module (org.spockframework:spock-core:2.4-M1-groovy-3.0)
- Spock Framework - JUnit 4 (org.spockframework:spock-junit4:2.4-M1-groovy-3.0)

**Eclipse Public License:**
- JUnit (junit:junit:4.13.2)
- JUnit Platform Commons (org.junit.platform:junit-platform-commons:1.9.0)
- JUnit Platform Engine API (org.junit.platform:junit-platform-engine:1.9.0)

---

## Docker Image Components

The Docker image distributed on Docker Hub includes the following additional components:

### Java Runtime Environment

**Eclipse Temurin OpenJDK (eclipse-temurin:21-jre-jammy)**
- License: GPLv2 with Classpath Exception
- Copyright: Contributors to the Eclipse Temurin project
- Source: https://adoptium.net/
- Full License: https://openjdk.org/legal/gplv2+ce.html

The GNU Classpath Exception permits programs licensed under MIT (like pfxprobe) to link to and run on the OpenJDK runtime without requiring the program itself to be licensed under GPL.

### Base Operating System

**Ubuntu 22.04 LTS (Jammy Jellyfish)**
- The Docker base image includes Ubuntu Linux and various system libraries
- Ubuntu and its components are distributed under various open source licenses including GPL, LGPL, Apache, and others
- Full Ubuntu licensing details: https://ubuntu.com/legal/intellectual-property-policy

---

### License Texts

#### Apache License 2.0
Full text: https://www.apache.org/licenses/LICENSE-2.0

#### BSD License
Full text: https://opensource.org/licenses/BSD-3-Clause

#### MIT License
Full text: https://opensource.org/licenses/MIT

#### Eclipse Public License
Full text: https://www.eclipse.org/legal/epl-2.0/

#### GPLv2 with Classpath Exception
Full text: https://openjdk.org/legal/gplv2+ce.html

---

**Note:** The dependency list above is automatically generated and maintained.
For the most current list, run: `mvn license:add-third-party`
