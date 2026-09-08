package ${package};

import io.github.dg.spring.inertia.api.Inertia;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    private final Inertia inertia;

    @Value("${appName}")
    private String appName;

    public WelcomeController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/")
    public Object welcome() {
        return inertia.render("Welcome", Map.of(
            "appName", appName,
            "framework", "Spring Boot",
            "frameworkVersion", "${frameworkVersion}",
            "inertiaUrl", "https://inertiajs.com/"
        ));
    }
}
