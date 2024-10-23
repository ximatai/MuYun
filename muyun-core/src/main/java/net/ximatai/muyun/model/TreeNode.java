package net.ximatai.muyun.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "树节点")
public class TreeNode {
    @Schema(description = "数据id")
    private String id;
    @Schema(description = "数据标题")
    private String label;
    @Schema(description = "数据内容")
    private Map<String, Object> data;
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

    public Map<String, Object> getData() {
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

    public TreeNode setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public TreeNode setChildren(List<? extends TreeNode> children) {
        this.children = (List<TreeNode>) children;
        return this;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeNode node) {
            return this.getId().equals(node.getId());
        } else {
            return false;
        }
    }

}
