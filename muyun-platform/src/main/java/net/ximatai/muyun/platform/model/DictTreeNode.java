package net.ximatai.muyun.platform.model;

import net.ximatai.muyun.model.TreeNode;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class DictTreeNode extends TreeNode {
    @Schema(description = "字典值")
    private String value;

    public static DictTreeNode from(TreeNode node) {
        return (DictTreeNode) new DictTreeNode()
            .setId(node.getId())
            .setLabel(node.getLabel())
            .setChildren(node.getChildren())
            .setData(node.getData());
    }

    public String getValue() {
        return value;
    }

    public DictTreeNode setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public List<DictTreeNode> getChildren() {
        return (List<DictTreeNode>) super.getChildren();
    }
}
