package ${package};

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory contact repository with seed data. A real application would
 * replace it with a database-backed implementation.
 */
@ApplicationScoped
public class ContactRepository {

    private final ConcurrentHashMap<Long, Contact> contacts = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public ContactRepository() {
        seed("Ada Lovelace", "ada@example.com", "+34 600 111 222");
        seed("Alan Turing", "alan@example.com", "+34 600 333 444");
        seed("Grace Hopper", "grace@example.com", "+34 600 555 666");
    }

    public List<Contact> findAll() {
        return contacts.values().stream()
            .sorted(Comparator.comparing(Contact::id))
            .toList();
    }

    public Contact find(long id) {
        return contacts.get(id);
    }

    public Contact create(ContactForm form) {
        var id = sequence.incrementAndGet();
        var contact = new Contact(id, normalized(form.name()), normalized(form.email()), normalized(form.phone()));
        contacts.put(id, contact);
        return contact;
    }

    public boolean update(long id, ContactForm form) {
        if (!contacts.containsKey(id)) {
            return false;
        }
        contacts.put(id, new Contact(id, normalized(form.name()), normalized(form.email()), normalized(form.phone())));
        return true;
    }

    public boolean delete(long id) {
        return contacts.remove(id) != null;
    }

    private void seed(String name, String email, String phone) {
        var id = sequence.incrementAndGet();
        contacts.put(id, new Contact(id, name, email, phone));
    }

    private static String normalized(String value) {
        return value == null ? null : value.trim();
    }
}
