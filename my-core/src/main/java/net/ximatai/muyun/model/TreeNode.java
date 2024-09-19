package net.ximatai.muyun.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "树节点")
public class TreeNode {
    @Schema(description = "数据id")
    private String id;
    @Schema(description = "数据标题")
    private String label;
    @Schema(description = "数据内容")
    private Object data;
    @Schema(description = "子节点")
    private List<TreeNode> children;

    public TreeNode() {
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Object getData() {
        return data;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public TreeNode setId(String id) {
        this.id = id;
        return this;
    }

    public TreeNode setLabel(String label) {
        this.label = label;
        return this;
    }

    public TreeNode setData(Object data) {
        this.data = data;
        return this;
    }

    public TreeNode setChildren(List<TreeNode> children) {
        this.children = children;
        return this;
    }
}
