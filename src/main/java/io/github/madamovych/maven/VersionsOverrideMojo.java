package io.github.madamovych.maven;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.github.madamovych.maven.util.AlphanumComparator;

/**
 * Analyze Maven project tree and answer the question how do versions change from top (parent POM) to bottom for dependencies<br>
 * 
 * <pre>
 * U upgraded
 * D downgraded
 * - unchanged
 * . undefined
 * </pre>
 * 
 * example output
 * 
 * <pre>
 * [INFO] D org.springframework:spring-core.jar 5.3.19 > - > 5.3.10 > -
 * [INFO] D org.springframework:spring-test.jar 5.3.19 > - > 5.3.10 > -
 * [INFO] D org.springframework:spring-web.jar 5.3.19 > - > 5.3.10 > -
 * </pre>
 * 
 * example commands
 * 
 * <pre>
 * # quick run for current project
 * mvn io.github.madamovych:maven-utilities-maven-plugin:1.0.0:versions-override
 * # generate for all sub-projects if agregator project contains plugin declaration 
 * mvn validate
 * # run for current project using plugin preffix if pluginGroup specified in maven settings.xml
 * mvn maven-utilities:versions-override
 * # optional properties
 * mvn maven-utilities:versions-override -DdeclarationOrder -Dscope=compile,provided
 * </pre>
 */
@Mojo(name = "versions-override", defaultPhase = VALIDATE)
public class VersionsOverrideMojo extends AbstractMojo {

	private static final String UPGRADED = "U";
	private static final String DOWNGRADED = "D";
	private static final String UNCHANGED = "-";
	private static final String UNDEFINED = ".";

	@Parameter(property = "scope")
	private String scope;
	@Parameter(property = "declarationOrder")
	private String declarationOrder;
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	private AlphanumComparator ac = new AlphanumComparator(true);

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		for (Dependency dependency : projectDependenciesFilteredSorted()) {
			String originalVersion = null;
			LinkedList<String> versions = new LinkedList<>();
			for (MavenProject parent = project.getParent(); parent != null; parent = parent.getParent()) {
				Optional<Dependency> overriden = parent.getDependencies().stream().filter(d -> ((Dependency) d).getManagementKey().equals(dependency.getManagementKey())).findFirst();
				if (!overriden.isPresent())
					overriden = parent.getDependencyManagement().getDependencies().stream().filter(d -> ((Dependency) d).getManagementKey().equals(dependency.getManagementKey())).findFirst();
				if (overriden.isPresent())
					originalVersion = overriden.get().getVersion();
				versions.addFirst(overriden.isPresent() ? overriden.get().getVersion() : null);
			}
			versions.addLast(dependency.getVersion());

			String state = originalVersion == null || dependency.getVersion().equals(originalVersion) ? UNCHANGED
					: ac.compare(dependency.getVersion(), originalVersion) > 0 ? UPGRADED : DOWNGRADED;

			StringBuilder versionsChange = new StringBuilder();
			versionsChange.append(Objects.toString(versions.get(0), UNDEFINED));
			for (int i = 1; i < versions.size(); i++) {
				versionsChange.append(format(" > %s", versions.get(i) == null ? UNDEFINED
						: versions.get(i).equals(versions.get(i - 1)) ? UNCHANGED : versions.get(i)));
			}

			getLog().info(format("%s %s %s", state, dependency.getManagementKey(), versionsChange));
		}
	}

	public List<Dependency> projectDependenciesFilteredSorted() {
		List<Dependency> dependencies = new ArrayList<>(project.getDependencies());
		if (scope != null) {
			HashSet<String> scopeFilter = new HashSet<>();
			scopeFilter.addAll(asList(scope.split(",")));
			for (Iterator<Dependency> iter = dependencies.iterator(); iter.hasNext();) {
				if (!scopeFilter.contains(iter.next().getScope()))
					iter.remove();
			}
		}
		if (declarationOrder == null)
			sort(dependencies, (d1, d2) -> ac.compare(d1.getManagementKey(), d2.getManagementKey()));
		return dependencies;
	}
}
