package net.ximatai.muyun.util;

import net.ximatai.muyun.model.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeBuilderTest {

    static List list = List.of(
        buildNode("A", null),
        buildNode("B", null),
        buildNode("C", null),
        buildNode("A.a", "A"),
        buildNode("A.b", "A"),
        buildNode("A.a.1", "A.a"),
        buildNode("C.a.1", "C.a"),
        buildNode("B.a", "B")
    );

    @Test
    void testTree() {
        List<TreeNode> tree = TreeBuilder.build("id", "pid", list, null, false, "name", 10);
        TreeNode nodeA = tree.get(0);
        assertEquals("A", nodeA.getLabel());
        assertEquals(2, nodeA.getChildren().size());
        assertEquals("A.a.1", nodeA.getChildren().get(0).getChildren().get(0).getLabel());
    }

    @Test
    void testTreeA() {
        List<TreeNode> tree = TreeBuilder.build("id", "pid", list, "A", true, "name", 10);
        TreeNode nodeA = tree.get(0);
        assertEquals("A", nodeA.getLabel());
        assertEquals(2, nodeA.getChildren().size());
        assertEquals("A.a.1", nodeA.getChildren().get(0).getChildren().get(0).getLabel());
    }

    @Test
    void testTreeANotShowMe() {
        List<TreeNode> tree = TreeBuilder.build("id", "pid", list, "A", false, "name", 10);
        assertEquals(2, tree.size());
        assertEquals("A.a.1", tree.get(0).getChildren().get(0).getLabel());
    }

    @Test
    void testTreeNotFound() {
        List<TreeNode> tree = TreeBuilder.build("id", "pid", list, "X", false, "name", 10);
        assertEquals(0, tree.size());
    }

    static Map buildNode(String id, String pid) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("pid", pid);
        map.put("name", id);
        return map;
    }
}
