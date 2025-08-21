# versions-override  
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.madamovych/maven-utilities-maven-plugin)](https://mvnrepository.com/artifact/io.github.madamovych/maven-utilities-maven-plugin)

Analyze Maven project tree and answer the question how do versions change from top (parent POM) to bottom for dependencies  

    U upgraded
    D downgraded
    - unchanged
    . undefined
 
> defaultPhase=VALIDATE

### example output

    D org.junit.jupiter:junit-jupiter-api:jar 5.8.2 > - > 5.8.1 > -
    D org.junit.jupiter:junit-jupiter:jar 5.8.2 > - > 5.8.1 > -
    - org.mvel:mvel2:jar . > . > 2.4.4.Final > -
    D org.springframework:spring-core:jar 5.3.19 > - > 5.3.10 > -
    D org.springframework:spring-test:jar 5.3.19 > - > 5.3.10 > -
    D org.springframework:spring-web:jar 5.3.19 > - > 5.3.10 > -
    U org.yaml:snakeyaml:jar 1.29 > - > 2.2 > -

### run from command line or declare in POM as

    mvn io.github.madamovych:maven-utilities-maven-plugin:x.x.x:versions-override

    <plugin>
        <groupId>io.github.madamovych</groupId>
        <artifactId>maven-utilities-maven-plugin</artifactId>
        <version>x.x.x</version>
        <executions>
            <execution>
                <id>versions-override</id>
                <goals>
                    <goal>versions-override</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

### parameters  

    declarationOrder - define to list dependencies in declaration order, alphabetically by default
    scope - comma separated list of scopes to filter, no filtering by default

### example commands  

    # quick run for current project
    mvn io.github.madamovych:maven-utilities-maven-plugin:x.x.x:versions-override
    
    # generate for all sub-projects if agregator project contains plugin declaration 
    mvn validate
    
    # run for current project using plugin preffix if pluginGroup specified in maven settings.xml
    mvn maven-utilities:versions-override
    
    # optional properties
    mvn maven-utilities:versions-override -DdeclarationOrder -Dscope=compile,provided
