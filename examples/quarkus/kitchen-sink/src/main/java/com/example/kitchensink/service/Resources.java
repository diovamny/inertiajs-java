package com.example.kitchensink.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializes entities into the snake_case JSON shapes of the demo's
 * Laravel API resources (ContactResource, NoteResource, OrganizationResource).
 */
public final class Resources {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private Resources() {
    }

    public static String dateTime(Instant instant) {
        return instant != null ? DATE_TIME.format(instant) : null;
    }

    public static String iso(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    public static Map<String, Object> organization(Object id, String name, Long contactsCount) {
        var map = new LinkedHashMap<String, Object>();
        map.put("id", id);
        map.put("name", name);
        if (contactsCount != null) {
            map.put("contacts_count", contactsCount);
        }
        return map;
    }

    public static Map<String, Object> contact(com.example.kitchensink.entity.Contact contact,
            Map<String, Object> organization) {
        var map = new LinkedHashMap<String, Object>();
        map.put("id", contact.id);
        map.put("first_name", contact.firstName);
        map.put("last_name", contact.lastName);
        map.put("email", contact.email);
        map.put("phone", contact.phone);
        map.put("organization", organization);
        map.put("is_favorite", contact.isFavorite);
        map.put("created_at", dateTime(contact.createdAt));
        return map;
    }

    public static Map<String, Object> note(com.example.kitchensink.entity.Note note,
            Map<String, Object> contact, Map<String, Object> user) {
        var map = new LinkedHashMap<String, Object>();
        map.put("id", note.id);
        map.put("body", note.body);
        map.put("contact", contact);
        map.put("user", user);
        map.put("created_at", dateTime(note.createdAt));
        return map;
    }

    public static Map<String, Object> user(Object id, String name, String email) {
        var map = new LinkedHashMap<String, Object>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        return map;
    }
}
