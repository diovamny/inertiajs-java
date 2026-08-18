package com.example.demo.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.github.javafaker.Faker;

import com.example.demo.entity.Person;
import com.example.demo.entity.Employee;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DataSeeder {

    @Inject
    @ConfigProperty(name = "demo.seed.enabled", defaultValue = "true")
    boolean enabled;

    @Inject
    @ConfigProperty(name = "demo.seed.count", defaultValue = "200000")
    int count;

    @Inject
    @ConfigProperty(name = "demo.seed.batch-size", defaultValue = "500")
    int batchSize;

    @Inject
    jakarta.persistence.EntityManager em;

    @Inject
    UserTransaction utx;

    void onStart(@Observes StartupEvent ev) {
        if (!enabled) return;

        var faker = new Faker(new java.util.Random(42));

        var personCount = em.createQuery("SELECT COUNT(p) FROM Person p", Long.class).getSingleResult();
        var empCount = em.createQuery("SELECT COUNT(e) FROM Employee e", Long.class).getSingleResult();

        if (personCount == 0) {
            seedBatch("persons", batchSize, count, (i, f) -> {
                var p = new Person();
                p.name = f.name().firstName();
                p.lastName = f.name().lastName();
                p.email = f.internet().emailAddress();
                p.phone = f.phoneNumber().phoneNumber();
                p.active = f.random().nextBoolean();
                p.createdAt = f.date().past(365, TimeUnit.DAYS).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                p.updatedAt = p.createdAt;
                em.persist(p);
            });
        }

        if (empCount == 0) {
            var departments = new String[]{
                "Engineering", "Sales", "Marketing", "HR", "Finance",
                "Legal", "Operations", "Support", "Design", "Product"
            };
            seedBatch("employees", batchSize, count, (i, f) -> {
                var e = new Employee();
                e.firstName = f.name().firstName();
                e.lastName = f.name().lastName();
                e.email = f.internet().emailAddress();
                e.salary = BigDecimal.valueOf(f.number().randomDouble(2, 30000, 200000));
                e.active = f.random().nextBoolean();
                e.department = departments[f.random().nextInt(departments.length)];
                e.hireDate = LocalDate.ofInstant(
                    f.date().past(365 * 5, TimeUnit.DAYS).toInstant(),
                    ZoneId.systemDefault());
                e.createdAt = f.date().past(365, TimeUnit.DAYS).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                e.updatedAt = e.createdAt;
                em.persist(e);
            });
        }
    }

    @FunctionalInterface
    interface BatchConsumer {
        void accept(int index, Faker faker);
    }

    private void seedBatch(String label, int batchSize, int total, BatchConsumer consumer) {
        System.out.println("Seeding " + total + " " + label + "...");
        var start = System.currentTimeMillis();
        var faker = new Faker(new java.util.Random(42));

        for (int batch = 0; batch < total; batch += batchSize) {
            int end = Math.min(batch + batchSize, total);
            try {
                utx.begin();
                for (int i = batch; i < end; i++) {
                    consumer.accept(i, faker);
                }
                em.flush();
                em.clear();
                utx.commit();
                System.out.println("  " + label + " seeded: " + end + "/" + total);
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception ex) {}
                throw new RuntimeException("Failed seeding " + label + " batch " + batch, e);
            }
        }
        System.out.println("Seeded " + total + " " + label + " in " +
            (System.currentTimeMillis() - start) + "ms");
    }
}
