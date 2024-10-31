package net.ximatai.muyun.model.code;

import net.ximatai.muyun.core.exception.MyException;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerateConfig {

    private List<ICodePart> codePartList = new ArrayList<>();

    public List<ICodePart> getCodePartList() {
        return codePartList;
    }

    public void setCodePartList(List<ICodePart> codePartList) {
        int serialIndex = -1;

        // 查找 SerialCodePart 在列表中的位置
        for (int i = 0; i < codePartList.size(); i++) {
            if (codePartList.get(i) instanceof SerialCodePart) {
                serialIndex = i;
                break;
            }
        }

        // 验证 SerialCodePart 必须是列表中的最后一部分
        if (serialIndex > -1 && serialIndex != codePartList.size() - 1) {
            throw new MyException("流水号必须放置与单号生成器的最后一部分");
        }

        this.codePartList = codePartList;
    }

    // 构造函数：初始化 codePartList
    public CodeGenerateConfig(List<ICodePart> codePartList) {
        setCodePartList(codePartList);
    }

    public CodeGenerateConfig(int serialWidth) {
        this.codePartList.add(new SerialCodePart(serialWidth));
    }

    public CodeGenerateConfig(String prefix, int serialWidth) {
        this.codePartList.add(new SimpleCodePart(prefix));
        this.codePartList.add(new SerialCodePart(serialWidth));
    }

    public CodeGenerateConfig(String prefix, boolean useDate, int serialWidth) {
        this.codePartList.add(new SimpleCodePart(prefix));
        if (useDate) {
            this.codePartList.add(new DateCodePart());
        }
        this.codePartList.add(new SerialCodePart(serialWidth));
    }

    public CodeGenerateConfig(String prefix, TransformCodePart transformCodePart, int serialWidth) {
        this.codePartList.add(new SimpleCodePart(prefix));
        this.codePartList.add(transformCodePart);
        this.codePartList.add(new SerialCodePart(serialWidth));
    }

    public CodeGenerateConfig(String prefix, TransformCodePart transformCodePart, boolean useDate, int serialWidth) {
        this.codePartList.add(new SimpleCodePart(prefix));
        this.codePartList.add(transformCodePart);
        if (useDate) {
            this.codePartList.add(new DateCodePart());
        }
        this.codePartList.add(new SerialCodePart(serialWidth));
    }
}

