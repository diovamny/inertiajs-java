package ${package};

/**
 * A contact record listed in the starter CRUD.
 *
 * @param id    the identifier
 * @param name  the contact name
 * @param email the contact email
 * @param phone the contact phone
 */
public record Contact(Long id, String name, String email, String phone) {
}
