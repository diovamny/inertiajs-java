package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

import io.vertx.ext.web.RoutingContext;

import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CursorPagination;
import com.example.kitchensink.service.Demo;
import com.example.kitchensink.service.Resources;
import com.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/features/data-loading")
@Blocking
public class DataLoadingController {

    @Inject
    Inertia inertia;

    @Inject
    ContactRepository contacts;

    @Inject
    CrmQueryService query;

    @Context
    io.vertx.ext.web.RoutingContext rc;

    @GET
    @Path("deferred-props")
    @Blocking
    public Uni<Object> deferredProps(@HeaderParam("X-Force-Success") String forceSuccess) {
        inertia.deferred("slowStats", () -> {
            Demo.sleepMillis(800);
            return Uni.createFrom().item(Map.of(
                "totalContacts", contacts.count(),
                "totalFavorites", contacts.countFavorites()));
        });
        inertia.deferred("heavy", "heavyData", () -> {
            Demo.sleepMillis(1500);
            return Uni.createFrom().item(latestContacts(5));
        });
        inertia.deferred("flakyReport", () -> {
            Demo.sleepMillis(600);
            if (forceSuccess == null) {
                return Uni.createFrom().failure(new RuntimeException("Upstream report service is unavailable."));
            }
            return Uni.createFrom().item(Map.of("value", Demo.randomInt(1000, 9999)));
        });
        inertia.rescue("flakyReport");

        return inertia.render("Features/DataLoading/DeferredProps", Map.of(
            "quickStat", "Loaded instantly"));
    }

    @GET
    @Path("partial-reloads")
    @Blocking
    public Uni<Object> partialReloads() {
        return inertia.render("Features/DataLoading/PartialReloads", Map.of(
            "users", List.of(
                Map.of("id", 1, "name", "Alice", "role", "Admin"),
                Map.of("id", 2, "name", "Bob", "role", "Editor"),
                Map.of("id", 3, "name", "Charlie", "role", "Viewer")),
            "stats", Map.of(
                "total", contacts.count(),
                "favorites", contacts.countFavorites()),
            "timestamp", Demo.now(),
            "randomNumber", Demo.randomInt(1, 1000)));
    }

    @GET
    @Path("infinite-scroll")
    @Blocking
    public Uni<Object> infiniteScroll(@QueryParam("favorites") @DefaultValue("false") String favoritesRaw,
            @QueryParam("cursor") String cursor) {
        var favorites = favoritesRaw != null && !favoritesRaw.isBlank()
            && !"0".equals(favoritesRaw) && !"false".equalsIgnoreCase(favoritesRaw);
        var offset = CursorPagination.decode(cursor);
        var total = favorites ? contacts.countFavorites() : contacts.count();
        var contactList = contacts.find("order by id desc").page((int) offset, 10).list();
        var items = new ArrayList<Object>();
        for (var c : contactList) {
            Contact contact = (Contact) c;
            if (favorites && !contact.isFavorite) {
                continue;
            }
            items.add(Resources.contact(contact, null));
        }
        var payload = CursorPagination.payload("/features/data-loading/infinite-scroll",
            items, offset, 10, total, Map.of("favorites", favorites ? "1" : ""));

        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("pageName", "cursor");
        metadata.put("previousPage", payload.get("prev_cursor"));
        metadata.put("nextPage", payload.get("next_cursor"));
        metadata.put("currentPage", offset == 0 ? 1 : cursor);
        inertia.scroll("contacts", payload, metadata);

        return inertia.render("Features/DataLoading/InfiniteScroll", Map.of("contacts", payload));
    }

    @GET
    @Path("when-visible")
    @Blocking
    public Uni<Object> whenVisible() {
        inertia.optional("section1", () -> {
            Demo.sleepMillis(500);
            return Uni.createFrom().item(latestContacts(3));
        });
        inertia.optional("section2", () -> {
            Demo.sleepMillis(800);
            return Uni.createFrom().item(Map.of(
                "totalContacts", contacts.count(),
                "generated", Demo.now()));
        });
        inertia.optional("section3", () -> {
            Demo.sleepMillis(600);
            return Uni.createFrom().item(favoriteContacts(5));
        });
        return inertia.render("Features/DataLoading/WhenVisible");
    }

    @GET
    @Path("polling")
    @Blocking
    public Uni<Object> polling() {
        Demo.sleepMillis(Demo.randomInt(500, 3000));
        return inertia.render("Features/DataLoading/Polling", Map.of(
            "currentTime", Demo.now(),
            "randomNumber", Demo.randomInt(1, 1000),
            "contactCount", contacts.count()));
    }

    @GET
    @Path("prop-merging")
    @Blocking
    public Uni<Object> propMerging(@HeaderParam("X-Inertia-Reset") String resetHeader,
            @HeaderParam("X-Inertia-Partial-Data") String partialData) {
        var pool = List.of(
            Map.of("id", 1, "name", "Alice"),
            Map.of("id", 2, "name", "Bob"),
            Map.of("id", 3, "name", "Charlie"),
            Map.of("id", 4, "name", "Diana"),
            Map.of("id", 5, "name", "Eve"));

        var resetProps = resetHeader != null && !resetHeader.isBlank()
            ? List.of(resetHeader.split(",")) : List.of();
        var isPartial = partialData != null;

        int count;
        if (resetProps.contains("contacts")) {
            count = 1;
        } else if (isPartial) {
            var session = rc.session();
            var previous = session != null ? session.get("propMergingCount") : null;
            var prev = previous instanceof Number n ? n.intValue() : 1;
            count = Math.min(prev + 1, 5);
        } else {
            count = 1;
        }
        if (rc.session() != null) {
            rc.session().put("propMergingCount", count);
        }

        var now = Demo.now();
        var notifications = List.of(Map.of(
            "id", Demo.randomInt(1000, 9999),
            "message", "Notification at " + Demo.time(),
            "type", Demo.random("info", "success", "warning", "error", "alert", "update", "reminder", "system", "promo", "social")));

        var activities = List.of(Map.of(
            "id", Demo.randomInt(1000, 9999),
            "action", Demo.random("created", "updated", "deleted", "restored", "archived", "published"),
            "subject", Demo.random("Contact", "Organization", "Note", "Invoice", "Report", "Task"),
            "time", Demo.time()));

        var contactsSlice = new ArrayList<Object>();
        for (int i = 0; i < count; i++) {
            var contact = pool.get(i);
            var item = new LinkedHashMap<String, Object>(contact);
            item.put("updated", Demo.time());
            contactsSlice.add(item);
        }

        inertia.merge("notifications", notifications);
        inertia.prepend("activities", activities);
        inertia.merge("contacts", contactsSlice, false, "id");

        return inertia.render("Features/DataLoading/PropMerging", Map.of(
            "timestamp", now));
    }

    @GET
    @Path("optional-props")
    @Blocking
    public Uni<Object> optionalProps() {
        inertia.optional("optionalData", () -> {
            Demo.sleepMillis(500);
            return Uni.createFrom().item(Map.of(
                "generatedAt", Demo.now(),
                "contacts", latestContacts(3)));
        });
        inertia.deferred("deferredData", () -> {
            Demo.sleepMillis(600);
            return Uni.createFrom().item(Map.of(
                "generatedAt", Demo.now(),
                "totalContacts", contacts.count()));
        });
        return inertia.render("Features/DataLoading/OptionalProps", Map.of(
            "regularData", Map.of(
                "timestamp", Demo.now(),
                "message", "This prop is always included in the response.")));
    }

    @GET
    @Path("once-props/{page}")
    @Blocking
    public Uni<Object> onceProps(@PathParam("page") @DefaultValue("1") int page) {
        inertia.once("staticData", () -> Uni.createFrom().item(Map.of(
            "generatedAt", Demo.now(),
            "randomId", Demo.randomInt(1000, 9999))));
        inertia.once("freshData", () -> Uni.createFrom().item(Map.of(
            "generatedAt", Demo.now(),
            "value", Demo.randomInt(1, 1000))));
        inertia.once("expiringData", () -> Uni.createFrom().item(Map.of(
            "generatedAt", Demo.now(),
            "value", Demo.randomInt(1, 1000))), null,
            java.time.Instant.now().plusSeconds(5));
        inertia.once("aliasedData", () -> Uni.createFrom().item(Map.of(
            "generatedAt", Demo.now(),
            "value", Demo.randomInt(1, 1000))), "shared-once-key");
        return inertia.render("Features/DataLoading/OnceProps", Map.of(
            "page", page,
            "dynamicData", Map.of(
                "timestamp", Demo.now(),
                "randomNumber", Demo.randomInt(1, 1000))));
    }

    @GET
    @Path("once-props")
    @Blocking
    public Uni<Object> oncePropsDefault() {
        return onceProps(1);
    }

    private Object latestContacts(int limit) {
        var list = contacts.find("order by id desc").page(0, limit).list();
        var items = new ArrayList<Object>();
        for (var c : list) {
            Contact contact = (Contact) c;
            items.add(Map.of("id", contact.id, "name", contact.getName()));
        }
        return items;
    }

    private Object favoriteContacts(int limit) {
        var list = contacts.find("isFavorite = true order by id desc").page(0, limit).list();
        var items = new ArrayList<Object>();
        for (var c : list) {
            Contact contact = (Contact) c;
            items.add(Map.of("id", contact.id, "name", contact.getName()));
        }
        return items;
    }
}