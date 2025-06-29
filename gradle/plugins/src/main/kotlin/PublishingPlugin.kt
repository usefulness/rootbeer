import org.gradle.api.Plugin
import org.gradle.api.Project
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.vanniktech.maven.publish")

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            coordinates(group.toString(), name, version.toString())

            signAllPublications()

            configureBasedOnAppliedPlugins()

            pom { pom ->
                pom.name.set("${project.group}:${project.name}")
                pom.description.set(project.description)
                pom.url.set("https://github.com/usefulness/rootbeer")
                pom.licenses { licenses ->
                    licenses.license { license ->
                        license.name.set("MIT")
                        license.url.set("https://github.com/usefulness/rootbeer/blob/master/LICENSE")
                    }
                }
                pom.developers { developers ->
                    developers.developer { developer ->
                        developer.id.set("mateuszkwiecinski")
                        developer.name.set("Mateusz Kwiecinski")
                        developer.email.set("36954793+mateuszkwiecinski@users.noreply.github.com")
                    }
                }
                pom.scm { scm ->
                    scm.connection.set("scm:git:github.com/usefulness/rootbeer.git")
                    scm.developerConnection.set("scm:git:ssh://github.com/usefulness/rootbeer.git")
                    scm.url.set("https://github.com/usefulness/rootbeer/tree/master")
                }
            }

        }
        extensions.configure<PublishingExtension> {
            with(repositories) {
                maven { maven ->
                    maven.name = "github"
                    maven.setUrl("https://maven.pkg.github.com/usefulness/rootbeer")
                    with(maven.credentials) {
                        username = "usefulness"
                        password = findConfig("GITHUB_TOKEN")
                    }
                }
            }
        }
    }

    private inline fun <reified T: Any> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String {
    return findProperty(key)?.toString() ?: System.getenv(key) ?: ""
}
