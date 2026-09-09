package ${package};

import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/")
public class WelcomeController {

    @Inject
    Inertia inertia;

    @ConfigProperty(name = "app.name", defaultValue = "${appName}")
    String appName;

    @GET
    public Uni<Object> welcome() {
        return inertia.render("Welcome", Map.of(
            "appName", appName,
            "framework", "Quarkus",
            "frameworkVersion", "${frameworkVersion}",
            "inertiaUrl", "https://inertiajs.com/"
        ));
    }
}
