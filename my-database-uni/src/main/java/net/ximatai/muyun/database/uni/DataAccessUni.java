package net.ximatai.muyun.database.uni;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import net.ximatai.muyun.database.IDatabaseAccessUni;
import net.ximatai.muyun.database.tool.TupleTool;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataAccessUni implements IDatabaseAccessUni {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    public Uni<String> insert(String sql, Map<String, Object> params, String pk) {
        return this.row(sql, params).map(row -> row.get(pk).toString());
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

}
