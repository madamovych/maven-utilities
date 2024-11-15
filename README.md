# versions-override  
defaultPhase=VALIDATE

Analyze Maven project tree and answer the question how do versions change from top (parent POM) to bottom for dependencies  

    U upgraded
    D downgraded
    - unchanged
    . undefined
 
example output

    [INFO] D org.springframework:spring-core [compile] 5.3.19 > - > 5.3.10 > -
    [INFO] D org.springframework:spring-test [provided] 5.3.19 > - > 5.3.10 > -
    [INFO] D org.springframework:spring-web [provided] 5.3.19 > - > 5.3.10 > -
 
parameters  

    declarationOrder - define to list dependencies in declaration order, alphabetically by default
    scope - comma separated list of scopes to filter, no filtering by default
 
example commands  

    # quick run for current project
    mvn com.github.madamovych:maven-utilities-maven-plugin:1.0.0:versions-override
    # generate for all sub-projects if agregator project contains plugin declaration with execution 
    mvn validate
    # run for current project using plugin preffix if pluginGroup specified in maven settings.xml
    mvn maven-utilities:versions-override
    # optional properties
    mvn maven-utilities:versions-override -DdeclarationOrder -Dscope=compile,provided
