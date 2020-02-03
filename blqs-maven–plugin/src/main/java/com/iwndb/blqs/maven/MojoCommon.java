package com.iwndb.blqs.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.iwdnb.blqs.core.utils.WildcardUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.dependency.utils.translators.ArtifactTranslator;
import org.apache.maven.plugins.dependency.utils.translators.ClassifierTypeTranslator;
import org.apache.maven.project.*;
import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;

import com.iwdnb.blqs.core.Options;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: diamondbc
 */
@Slf4j
public class MojoCommon {

    public static List<String> whiteEnvList = Arrays.asList("daily", "project");

    public static void resolveJarPaths(Options options, List<Dependency> dependencyList, MavenProject project,
                                       ProjectBuilder projectBuilder, MavenSession session,
                                       ArtifactHandlerManager artifactHandlerManager,
                                       List<ArtifactRepository> remoteRepositories, ArtifactResolver artifactResolver) {
        // org.eclipse.aether.artifact.Artifact aaa;
        List<String> jarPathList = new ArrayList<>();
        for (Artifact artifact : project.getArtifacts()) {
            if (contains(artifact, options.getWhiteJarList())) {
                translateSource(artifactHandlerManager, artifact, session, remoteRepositories, artifactResolver);
                String pathStr = artifact.getFile().getAbsolutePath();
                jarPathList.add(pathStr);
                Path path = resolve(pathStr, project);
                options.jar(path);
                // 添加source依赖
                addSource(path, options, project);
            }
        }
        for (String jarPath : jarPathList) {
            log.info("load jar:" + jarPath);
        }
        if (CollectionUtils.isNotEmpty(dependencyList)) {
            for (Dependency dependency : dependencyList) {
                String path = dependency.getGroupId() + "/" + dependency.getArtifactId();
                path = path.replaceAll("\\.", "/");
                if (!jarPathList.contains(path)) {
                    resolveJarPath(options, dependency, project);
                }
            }
        }

    }

    public static void resolveJarPath(Options options, Dependency dependency, MavenProject project) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.getArtifactId().equalsIgnoreCase(artifactId)
                && artifact.getGroupId().equalsIgnoreCase(groupId)) {
                Path path = resolve(artifact.getFile().getAbsolutePath(), project);
                options.jar(path);
                // 添加source依赖
                addSource(path, options, project);
                return;
            }
        }
    }

    private static void addSource(Path path, Options options, MavenProject project) {
        String pathStr = path.toString();
        pathStr = pathStr.substring(0, pathStr.indexOf(".jar")) + "-sources";
        String sourcePathStr = pathStr + ".jar";
        Path sourcePath = resolve(sourcePathStr, project);
        if (!Files.exists(sourcePath)) {
            return;
        }
        try {
            Decompression.uncompress(new File(sourcePathStr), new File((pathStr)));
        } catch (IOException e) {

        }
        options.dependency(resolve(pathStr, project));
        log.info("load source jar:" + sourcePathStr);
    }

    public static boolean contains(Artifact artifact, List<String> whiteJarList) {
        String path = artifact.getGroupId() + ":" + artifact.getArtifactId();
        if (WildcardUtils.match(path, whiteJarList)) {
            return true;
        }
        if (artifact.getArtifactId().endsWith("spi") && artifact.getArtifactId().endsWith("api")
            && artifact.getGroupId().startsWith("com.wdnb")) {
            return true;
        }
        return false;
    }

    public static Path resolve(String dir, MavenProject project) {
        Path path = Paths.get(dir);
        if (path.isAbsolute()) {
            return path;
        } else {
            return project.getBasedir().toPath().resolve(path);
        }
    }

    private static MavenProject buildProjectFromArtifact(Artifact artifact, ProjectBuilder projectBuilder,
                                                         MavenSession session) {
        try {
            log.info("buildProjectFromArtifact:{},{},{}", artifact, projectBuilder, session);
            return projectBuilder.build(artifact, session.getProjectBuildingRequest()).getProject();
        } catch (ProjectBuildingException e) {
            log.info("buildProjectFromArtifact wrong:" + artifact);
            return null;
        }

    }

    private static void translateSource(ArtifactHandlerManager artifactHandlerManager, Artifact artifact,
                                        MavenSession session, List<ArtifactRepository> remoteRepositories,
                                        ArtifactResolver artifactResolver) {
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        ArtifactTranslator translator = new ClassifierTypeTranslator(artifactHandlerManager, "sources", "");
        Collection<ArtifactCoordinate> coordinates = translator.translate(artifacts, getLog());
        Set<Artifact> resolveArtifacts = resolve(new LinkedHashSet<ArtifactCoordinate>(coordinates), session,
                                                 remoteRepositories, artifactResolver);
        log.info("translate source:{}", resolveArtifacts);
    }

    /**
     * @param coordinates The set of artifact coordinates{@link ArtifactCoordinate}.
     * @return the resolved artifacts. {@link Artifact}.
     * @throws MojoExecutionException in case of error.
     */
    protected static Set<Artifact> resolve(Set<ArtifactCoordinate> coordinates, MavenSession session,
                                           List<ArtifactRepository> remoteRepositories,
                                           ArtifactResolver artifactResolver) {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setRemoteRepositories(remoteRepositories);

        Set<Artifact> resolvedArtifacts = new LinkedHashSet<Artifact>();
        for (ArtifactCoordinate coordinate : coordinates) {
            try {
                Artifact artifact = artifactResolver.resolveArtifact(buildingRequest, coordinate).getArtifact();
                resolvedArtifacts.add(artifact);
            } catch (ArtifactResolverException ex) {
                // an error occurred during resolution, log it an continue
                log.debug("error resolving: " + coordinate);
            }
        }
        return resolvedArtifacts;
    }

    private static Log getLog() {
        return new SystemStreamLog();
    }

}
