package net.ximatai.muyun.database.uni;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import net.ximatai.muyun.database.DBInfoProvider;
import net.ximatai.muyun.database.uni.tool.TupleTool;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataAccessUni extends DBInfoProvider implements IDatabaseAccessUni {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    public <T> Uni<T> insert(String sql, Map<String, Object> params, String pk, Class<T> idType) {
        return this.row(sql + " returning " + pk, params)
            .map(Unchecked.function(row -> {
                Object value = row.get(pk);
                if (idType.isInstance(value)) {
                    return idType.cast(value);
                } else {
                    throw new IllegalArgumentException("The returned value type does not match the expected type: " + idType.getName());
                }
            }));
    }

    @Override
    public Uni<Map<String, Object>> row(String sql, Map<String, Object> params) {
        return sessionFactory.withSession(session -> {

            Mutiny.SelectionQuery<Tuple> query = session.createNativeQuery(sql, Tuple.class);

            if (params != null) {
                params.forEach(query::setParameter);
            }

            return query
                .getSingleResult()
                .map(TupleTool::toMap)
                .onFailure(NoResultException.class)
                .recoverWithItem(() -> null);
        });
    }

    @Override
    public Uni<Map<String, Object>> row(String sql) {
        return null;
    }

    @Override
    public Uni<List<Map<String, Object>>> query(String sql, Map<String, Object> params) {
        return null;
    }

    @Override
    public Uni<List<Map<String, Object>>> query(String sql) {
        return null;
    }

    @Override
    public Uni<Integer> update(String sql, Map<String, Object> params) {
        return null;
    }

    @Override
    public Uni<Integer> delete(String sql, Map<String, Object> params) {
        return null;
    }

    @Override
    public Uni<Void> execute(String sql) {
        return sessionFactory.withTransaction((session, tx) ->
            session.createNativeQuery(sql).executeUpdate()
        ).replaceWithVoid();
    }

}
