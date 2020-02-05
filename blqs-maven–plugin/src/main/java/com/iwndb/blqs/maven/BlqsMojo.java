package com.iwndb.blqs.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;

import com.iwdnb.blqs.core.BlqsContext;
import com.iwdnb.blqs.core.Options;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mojo(name = "yapi", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class BlqsMojo extends AbstractMojo {

    MavenProject                     project;

    @Component
    protected ProjectBuilder         projectBuilder;

    @Component
    private ArtifactHandlerManager   artifactHandlerManager;
    @Component
    private ArtifactResolver         artifactResolver;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession           session;

    @Parameter
    String                           id;

    @Parameter
    String                           title;

    @Parameter
    String                           description;

    @Parameter
    String                           out;

    @Parameter
    String                           production;

    @Parameter
    String                           source;

    @Parameter
    String                           dependency;

    @Parameter
    String                           ignore;

    @Parameter
    String                           version;

    @Parameter
    String                           css;
    @Parameter(defaultValue = "${incluedArtifacts}")
    String                           incluedArtifacts;
    @Parameter(defaultValue = "${yapiToken}")
    String                           yapiToken;

    @Parameter(defaultValue = "${yapiSynDataUrl}")
    String                           yapiSynDataUrl;

    @Parameter(defaultValue = "${yapiBatch}")
    Integer                          yapiBatch;

    @Parameter(defaultValue = "${blqsEnable}")
    boolean                          blqsEnable       = false;

    @Parameter(defaultValue = "${yapiMockParam}")
    String                           yapiMockParam;
    @Parameter(defaultValue = "${urls}")
    String                           urls;
    @Parameter(defaultValue = "${ignoreParameterClasses}")
    String                           ignoreParameterClasses;
    String                           defaultMockParam = "code:000000,isSuccess:true,message:你瞅啥,";

    @Override
    public void execute() {
        try {
            log.info("build yapi doc by blqs");
            String env = System.getenv("ENV_TYPE") != null ? System.getenv("ENV_TYPE").toLowerCase() : "";
            log.info("env:{},blqsEnable:{}", env, blqsEnable);
            // project/daily环境或者enable==true时可以执行
            if (!(MojoCommon.whiteEnvList.contains(env) || blqsEnable)) {
                log.info("disable execute blqs-maven-plugin,return");
                return;
            }
            if (getPluginContext().containsKey("project")
                && getPluginContext().get("project") instanceof MavenProject) {
                project = (MavenProject) getPluginContext().get("project");
                build();
            }
        } catch (Exception e) {
            log.error("build blqs error", e);
            throw e;
        }

    }

    private void build() {
        if (yapiSynDataUrl == null) {
            log.error("yapiSynDataUrl is null.");
            return;
        }
        Options options = new Options();
        // 加载依赖资源jar包
        if (incluedArtifacts != null) {
            options.incluedArtifacts(incluedArtifacts);
        }
        PluginDescriptor pluginDescriptor = (PluginDescriptor) this.getPluginContext().get("pluginDescriptor");
        List<Dependency> dependencyList = pluginDescriptor.getPlugin().getDependencies();
        MojoCommon.resolveJarPaths(options, dependencyList, project, projectBuilder, session, artifactHandlerManager,
                                   remoteRepositories, artifactResolver);

        if (source != null) {
            for (String dir : source.split(",")) {
                Path path = resolve(dir);
                options.source(path);
            }
        } else {
            options.source(Paths.get(project.getBuild().getSourceDirectory()));
            if (project.getCollectedProjects() != null) {
                for (MavenProject sub : project.getCollectedProjects()) {
                    options.source(Paths.get(sub.getBuild().getSourceDirectory()));
                }
            }
        }
        if (dependency != null) {
            String[] dirs = dependency.split(",");
            for (String dir : dirs) {
                Path path = resolve(dir);
                options.dependency(path);
            }
        } else {
            if (project.getParent() != null && project.getParent().getCollectedProjects() != null) {
                for (MavenProject p : project.getParent().getCollectedProjects()) {
                    String path = p.getBuild().getSourceDirectory();
                    options.dependency(Paths.get(path));
                }
            }
        }

        if (id != null) {
            options.id(id);
        } else {
            options.id(project.getName());
        }
        if (production != null) {
            options.production(Paths.get(production));
        }
        if (out != null) {
            Path path = resolve(out);
            options.out(path);
        } else {
            options.out(Paths.get(project.getBuild().getDirectory()));
        }
        if (title != null) {
            options.title(title);
        } else {
            options.title(project.getName());
        }
        if (description != null) {
            options.description(description);
        } else if (project.getDescription() != null) {
            options.description(project.getDescription());
        }
        if (version != null) {
            options.version(version);
        } else if (project.getVersion() != null) {
            options.version(project.getVersion());
        }
        if (ignore != null) {
            options.ignore(ignore.split(","));
        }
        if (css != null) {
            options.css(css);
        }
        if (yapiToken != null) {
            options.yapiToken(yapiToken);
        }
        if (yapiSynDataUrl != null) {
            options.yapiSynDataUrl(yapiSynDataUrl);
        }
        if (yapiBatch != null) {
            options.yapiBatch(yapiBatch);
        }
        if (yapiMockParam != null) {
            defaultMockParam += yapiMockParam;
        }
        if (urls != null) {
            options.urls(urls);
        }
        if (ignoreParameterClasses != null) {
            options.ignoreParameterClasses(ignoreParameterClasses);
        }
        options.yapiMockParam(defaultMockParam);
        new BlqsContext(options).lookup().build();

    }

    private Path resolve(String dir) {
        Path path = Paths.get(dir);
        if (path.isAbsolute()) {
            return path;
        } else {
            return project.getBasedir().toPath().resolve(path);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getIgnore() {
        return ignore;
    }

    public void setIgnore(String ignore) {
        this.ignore = ignore;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public String getProduction() {
        return production;
    }

    public String getYapiToken() {
        return yapiToken;
    }

    public void setYapiToken(String yapiToken) {
        this.yapiToken = yapiToken;
    }

    public String getYapiSynDataUrl() {
        return yapiSynDataUrl;
    }

    public void setYapiSynDataUrl(String yapiSynDataUrl) {
        this.yapiSynDataUrl = yapiSynDataUrl;
    }

    public Integer getYapiBatch() {
        return yapiBatch;
    }

    public void setYapiBatch(Integer yapiBatch) {
        this.yapiBatch = yapiBatch;
    }

    public String getYapiMockParam() {
        return yapiMockParam;
    }

    public void setYapiMockParam(String yapiMockParam) {
        this.yapiMockParam = yapiMockParam;
    }
}
