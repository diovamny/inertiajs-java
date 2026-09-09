package com.example.kitchensink.controller.feature;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CursorPagination;
import com.example.kitchensink.service.Demo;
import com.example.kitchensink.service.Resources;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class DataLoadingController {

    private final Inertia inertia;
    private final ContactRepository contacts;
    private final CrmQueryService query;

    public DataLoadingController(Inertia inertia, ContactRepository contacts, CrmQueryService query) {
        this.inertia = inertia;
        this.contacts = contacts;
        this.query = query;
    }

    @GetMapping("/features/data-loading/deferred-props")
    public Object deferredProps(@RequestHeader(value = "X-Force-Success", required = false) String forceSuccess) {
        inertia.deferred("slowStats", "slowStats", () -> {
            Demo.sleepMillis(800);
            return Map.of(
                "totalContacts", contacts.count(),
                "totalFavorites", contacts.countByIsFavorite(true));
        });
        inertia.deferred("heavy", "heavyData", () -> {
            Demo.sleepMillis(1500);
            return latestContacts(5);
        });
        inertia.deferred("flakyReport", "flakyReport", () -> {
            Demo.sleepMillis(600);
            if (forceSuccess == null) {
                throw new RuntimeException("Upstream report service is unavailable.");
            }
            return Map.of("value", Demo.randomInt(1000, 9999));
        });
        inertia.rescue("flakyReport");

        return inertia.render("Features/DataLoading/DeferredProps", Map.of(
            "quickStat", "Loaded instantly"));
    }

    @GetMapping("/features/data-loading/partial-reloads")
    public Object partialReloads() {
        return inertia.render("Features/DataLoading/PartialReloads", Map.of(
            "users", List.of(
                Map.of("id", 1, "name", "Alice", "role", "Admin"),
                Map.of("id", 2, "name", "Bob", "role", "Editor"),
                Map.of("id", 3, "name", "Charlie", "role", "Viewer")),
            "stats", Map.of(
                "total", contacts.count(),
                "favorites", contacts.countByIsFavorite(true)),
            "timestamp", Demo.now(),
            "randomNumber", Demo.randomInt(1, 1000)));
    }

    @GetMapping("/features/data-loading/infinite-scroll")
    public Object infiniteScroll(@RequestParam(value = "favorites", defaultValue = "false") String favoritesRaw,
            @RequestParam(value = "cursor", required = false) String cursor) {
        var favorites = favoritesRaw != null && !favoritesRaw.isBlank()
            && !"0".equals(favoritesRaw) && !"false".equalsIgnoreCase(favoritesRaw);
        var offset = CursorPagination.decode(cursor);
        var total = favorites ? contacts.countByIsFavorite(true) : contacts.count();
        var all = contacts.findAllByOrderByIdDesc();
        var from = (int) Math.min(offset, all.size());
        var to = (int) Math.min(offset + 10, all.size());
        var items = new ArrayList<Object>();
        for (var contact : all.subList(from, to)) {
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

    @GetMapping("/features/data-loading/when-visible")
    public Object whenVisible() {
        var props = new LinkedHashMap<String, Object>();
        props.put("section1", inertia.optional("section1", () -> {
            Demo.sleepMillis(500);
            return latestContacts(3);
        }));
        props.put("section2", inertia.optional("section2", () -> {
            Demo.sleepMillis(800);
            return Map.of(
                "totalContacts", contacts.count(),
                "generated", Demo.now());
        }));
        props.put("section3", inertia.optional("section3", () -> {
            Demo.sleepMillis(600);
            return favoriteContacts(5);
        }));
        return inertia.render("Features/DataLoading/WhenVisible", props);
    }

    @GetMapping("/features/data-loading/polling")
    public Object polling() {
        Demo.sleepMillis(Demo.randomInt(500, 3000));
        return inertia.render("Features/DataLoading/Polling", Map.of(
            "currentTime", Demo.now(),
            "randomNumber", Demo.randomInt(1, 1000),
            "contactCount", contacts.count()));
    }

    @GetMapping("/features/data-loading/prop-merging")
    public Object propMerging(@RequestHeader(value = "X-Inertia-Reset", required = false) String resetHeader,
            @RequestHeader(value = "X-Inertia-Partial-Data", required = false) String partialData,
            HttpSession session) {
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
            var previous = session.getAttribute("propMergingCount");
            var prev = previous instanceof Number n ? n.intValue() : 1;
            count = Math.min(prev + 1, 5);
        } else {
            count = 1;
        }
        session.setAttribute("propMergingCount", count);

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
        inertia.merge("activities", activities, Inertia.MergeRule.PREPEND);
        inertia.merge("contacts", contactsSlice, Inertia.MergeRule.MERGE, "id");

        return inertia.render("Features/DataLoading/PropMerging", Map.of(
            "timestamp", now,
            "notifications", notifications,
            "activities", activities,
            "contacts", contactsSlice));
    }

    @GetMapping("/features/data-loading/optional-props")
    public Object optionalProps() {
        var props = new LinkedHashMap<String, Object>();
        props.put("optionalData", inertia.optional("optionalData", () -> {
            Demo.sleepMillis(500);
            return Map.of(
                "generatedAt", Demo.now(),
                "contacts", latestContacts(3));
        }));
        inertia.deferred("deferredData", "deferredData", () -> {
            Demo.sleepMillis(600);
            return Map.of(
                "generatedAt", Demo.now(),
                "totalContacts", contacts.count());
        });
        props.put("regularData", Map.of(
            "timestamp", Demo.now(),
            "message", "This prop is always included in the response."));
        return inertia.render("Features/DataLoading/OptionalProps", props);
    }

    @GetMapping({"/features/data-loading/once-props", "/features/data-loading/once-props/{page}"})
    public Object onceProps(@PathVariable(value = "page", required = false) Integer page) {
        return inertia.render("Features/DataLoading/OnceProps", Map.of(
            "page", page != null ? page : 1,
            "staticData", inertia.once("staticData", Map.of(
                "generatedAt", Demo.now(),
                "randomId", Demo.randomInt(1000, 9999))),
            "freshData", inertia.once("freshData", Map.of(
                "generatedAt", Demo.now(),
                "value", Demo.randomInt(1, 1000))),
            "expiringData", inertia.once("expiringData", Map.of(
                "generatedAt", Demo.now(),
                "value", Demo.randomInt(1, 1000)), Duration.ofSeconds(5)),
            "aliasedData", inertia.once("aliasedData", Map.of(
                "generatedAt", Demo.now(),
                "value", Demo.randomInt(1, 1000)), "shared-once-key"),
            "dynamicData", Map.of(
                "timestamp", Demo.now(),
                "randomNumber", Demo.randomInt(1, 1000))));
    }

    private Object latestContacts(int limit) {
        var list = contacts.findAllByOrderByIdDesc();
        var items = new ArrayList<Object>();
        for (var contact : list.subList(0, Math.min(limit, list.size()))) {
            items.add(Map.of("id", contact.id, "name", contact.getName()));
        }
        return items;
    }

    private Object favoriteContacts(int limit) {
        var list = contacts.findAllByOrderByIdDesc();
        var items = new ArrayList<Object>();
        for (var contact : list) {
            if (items.size() >= limit) break;
            if (!contact.isFavorite) continue;
            items.add(Map.of("id", contact.id, "name", contact.getName()));
        }
        return items;
    }
}
