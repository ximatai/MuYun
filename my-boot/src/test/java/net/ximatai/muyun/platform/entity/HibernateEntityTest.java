package net.ximatai.muyun.platform.entity;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
@Transactional
class HibernateEntityTest {

    @Inject
    EntityManager entityManager;

    @Test
    void createEntityTest() {
        TestEntity e = new TestEntity();
        e.name = "test1";

        entityManager.persist(e);

        entityManager.flush();

        assertNotNull(e.id);
        assertEquals(e.id.length(), UUID.randomUUID().toString().length());
        assertNotNull(e.tCreate);
    }

}
