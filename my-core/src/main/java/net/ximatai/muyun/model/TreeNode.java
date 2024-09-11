package net.ximatai.muyun.model;

import java.util.List;

public class TreeNode {
    private String id;
    private String label;
    private Object data;
    private List<TreeNode> children;

    public TreeNode() {
    }

    public String id() {
        return id;
    }

    public TreeNode setId(String id) {
        this.id = id;
        return this;
    }

    public String label() {
        return label;
    }

    public TreeNode setLabel(String label) {
        this.label = label;
        return this;
    }

    public Object data() {
        return data;
    }

    public TreeNode setData(Object data) {
        this.data = data;
        return this;
    }

    public List<TreeNode> children() {
        return children;
    }

    public TreeNode setChildren(List<TreeNode> children) {
        this.children = children;
        return this;
    }
}
