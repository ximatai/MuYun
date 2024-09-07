import net.ximatai.muyun.database.std.argument.List2JsonArgumentFactory;
import net.ximatai.muyun.database.std.argument.Map2JsonArgumentFactory;
import net.ximatai.muyun.database.std.mapper.MyPgMapMapper;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JdbiTest {
    public static void main(String[] args) {
        // 创建 Jdbi 实例并安装 Postgres 插件
        Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:54324/muyun", "postgres", "muyun2024")
//            .installPlugin(new PostgresPlugin())
            .registerArgument(new Map2JsonArgumentFactory())
            .registerArgument(new List2JsonArgumentFactory());
//            .registerRowMapper(new MyPgMapMapper())
//            .registerArrayType(String.class, "varchar")
//            .registerColumnMapper(PgArray.class, new PgArrayToListMapper());

        // 使用 try-with-resources 确保资源自动释放
        try (Handle handle = jdbi.open()) {
            // 如果表尚未创建，则执行表创建 SQL（可选）
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS your_table (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100),
                j_text jsonb,
                colors VARCHAR[])""";
            handle.execute(createTableSql);

            // 插入操作
            String insertSql = "INSERT INTO your_table (name, colors,j_text) VALUES (:name, :colors,:j_text)";

            // 准备数据
            String name = "John Doe";
            List<String> colors = Arrays.asList("red", "blue");

            String[] colors2 = {"a", "b", "c"};
            // 执行插入
            handle.createUpdate(insertSql)
                .bind("name", name)    // 自动绑定 name 参数
//                .bindByType("colors", colors, new GenericType<List<String>>() {
//                }) // 显式指定类型
                .bind("colors", colors.toArray(new String[0]))
//                .bind("j_text","{\"a\":1,\"b\":2}")
                .bind("j_text", List.of("a", "b", "x", 1))
                .execute(); // 执行 SQL 语句

            System.out.println("数据插入成功！");

            // 查询操作，结果映射为 Map
            String selectSql = "SELECT id, name, colors,j_text FROM your_table";

            // 执行查询
            List<Map<String, Object>> results = handle.createQuery(selectSql)
//                .mapToMap()
                .map(new MyPgMapMapper())
                // 将结果直接映射为 List<Map<String, Object>>
                .list();

            // 打印查询结果
            for (Map<String, Object> row : results) {
                System.out.println(row.get("colors").getClass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
