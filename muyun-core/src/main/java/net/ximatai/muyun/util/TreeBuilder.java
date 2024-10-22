package net.ximatai.muyun.util;

import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.model.TreeNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TreeBuilder {

    private TreeBuilder() {
    }

    public static final String ROOT_PID = "__ROOT__";

    public static List<TreeNode> build(String pkColumn, String parentKeyColumn, List<Map<String, Object>> list, String rootID, boolean showMe, String labelColumn, Integer maxLevel) {
        if (maxLevel == null) {
            maxLevel = Integer.MAX_VALUE;
        }

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        list.forEach(item -> {
            item.putIfAbsent(parentKeyColumn, ROOT_PID);
        });

        if (rootID == null) {
            rootID = ROOT_PID;
        }

        // 按父键列（`parentKeyColumn`）分组
        Map<String, List<Map<String, Object>>> groupedByParentKey = list.stream()
            .collect(Collectors.groupingBy(item -> (String) item.get(parentKeyColumn)));

        Set<TreeNode> nodes = new HashSet<>();

        // 构建树形结构的递归方法
        List<TreeNode> treeNodeList = buildChildren(groupedByParentKey, pkColumn, parentKeyColumn, rootID, labelColumn, 1, maxLevel, nodes);

        if (showMe) {
            if (rootID.equals(ROOT_PID)) {
                throw new RuntimeException("showMe为true时，必须提供rootID");
            }
            String finalRootID = rootID;
            Map rootNode = list.stream().filter(it -> finalRootID.equals(it.get(pkColumn))).findFirst().orElseThrow(() -> new RuntimeException("rootID not found"));
            return Collections.singletonList(new TreeNode()
                .setId((String) rootNode.get(pkColumn)).setLabel((String) rootNode.get(labelColumn)).setData(rootNode)
                .setChildren(treeNodeList));
        } else {
            return treeNodeList;
        }
    }

    private static List<TreeNode> buildChildren(Map<String, List<Map<String, Object>>> groupedByParentKey, String pkColumn, String parentKeyColumn,
                                                String currentRootID, String labelColumn, int currentLevel, int maxLevel, Set<TreeNode> nodes) {
        if (currentLevel > maxLevel) {
            return Collections.emptyList(); // 超过最大层级，返回空列表
        }

        // 获取当前层级的节点列表
        List<Map<String, Object>> currentLevelNodes = groupedByParentKey.getOrDefault(currentRootID, Collections.emptyList());

        // 对当前层级的节点列表进行映射
        List<TreeNode> children = currentLevelNodes.stream().map(node -> {
            TreeNode treeNode = new TreeNode()
                .setId((String) node.get(pkColumn))
                .setLabel((String) node.get(labelColumn))
                .setData(node);

            if (nodes.contains(treeNode)) {
                throw new MyException("树节点出现递归调用");
            } else {
                nodes.add(treeNode);
            }

            // 递归构建子节点
            List<TreeNode> childNodes = buildChildren(groupedByParentKey, pkColumn, parentKeyColumn,
                (String) node.get(pkColumn), labelColumn, currentLevel + 1, maxLevel, nodes);
            treeNode.setChildren(childNodes);

            return treeNode;
        }).collect(Collectors.toList());

        if (children.isEmpty()) {
            return null;
        }

        return children;
    }
}
