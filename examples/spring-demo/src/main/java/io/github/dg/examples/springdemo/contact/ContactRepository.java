package io.github.dg.examples.springdemo.contact;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class ContactRepository {

    public record Page(List<Contact> data, long total, int page, int perPage, int lastPage) {

        static Page of(List<Contact> data, long total, int page, int perPage) {
            int lastPage = Math.max(1, (int) Math.ceil((double) total / perPage));
            return new Page(data, total, page, perPage, lastPage);
        }
    }

    private final Map<Long, Contact> store = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public ContactRepository() {
        seed();
    }

    public long count() {
        return store.size();
    }

    public long nextId() {
        return ids.incrementAndGet();
    }

    public List<Contact> all() {
        return store.values().stream()
            .sorted(Comparator.comparing(Contact::name))
            .toList();
    }

    public Optional<Contact> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Page findAll(String search, int page, int perPage) {
        var query = search == null ? "" : search.trim().toLowerCase();
        var filtered = store.values().stream()
            .filter(c -> query.isBlank()
                || c.name().toLowerCase().contains(query)
                || c.email().toLowerCase().contains(query))
            .sorted(Comparator.comparing(Contact::name))
            .collect(Collectors.toList());
        int from = Math.min((page - 1) * perPage, filtered.size());
        int to = Math.min(from + perPage, filtered.size());
        return Page.of(filtered.subList(from, to), filtered.size(), page, perPage);
    }

    public void save(Contact contact) {
        store.put(contact.id(), contact);
    }

    public void delete(long id) {
        store.remove(id);
    }

    private void seed() {
        String[][] rows = {
            { "Ada Lovelace", "ada@example.com", "555-0101" },
            { "Grace Hopper", "grace@example.com", "555-0102" },
            { "Alan Turing", "alan@example.com", "555-0103" },
            { "Edsger Dijkstra", "edsger@example.com", "555-0104" },
            { "Donald Knuth", "donald@example.com", "555-0105" },
            { "Barbara Liskov", "barbara@example.com", "555-0106" },
            { "Linus Torvalds", "linus@example.com", "555-0107" },
            { "Margaret Hamilton", "margaret@example.com", "555-0108" },
            { "Ken Thompson", "ken@example.com", "555-0109" },
            { "Frances Allen", "frances@example.com", "555-0110" },
            { "John McCarthy", "john@example.com", "555-0111" },
            { "Radia Perlman", "radia@example.com", "555-0112" },
            { "Richard Stallman", "richard@example.com", "555-0113" },
            { "Katherine Johnson", "katherine@example.com", "555-0114" },
            { "Dennis Ritchie", "dennis@example.com", "555-0115" },
            { "Guido van Rossum", "guido@example.com", "555-0116" },
            { "Bjarne Stroustrup", "bjarne@example.com", "555-0117" },
            { "Brendan Eich", "brendan@example.com", "555-0118" },
            { "James Gosling", "james@example.com", "555-0119" },
            { "Tim Berners-Lee", "tim@example.com", "555-0120" },
            { "Yukihiro Matsumoto", "yukihiro@example.com", "555-0121" },
            { "Hedy Lamarr", "hedy@example.com", "555-0122" },
            { "Norman Borlaug", "norman@example.com", "555-0123" },
            { "Sofia Kovalevskaya", "sofia@example.com", "555-0124" }
        };
        for (String[] row : rows) {
            save(new Contact(ids.incrementAndGet(), row[0], row[1], row[2]));
        }
    }
}