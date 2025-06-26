package net.ximatai.muyun.ability;

import net.ximatai.muyun.core.security.AbstractEncryptor;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 安全能力（数据加密、数据签名验证完整性）
 */
public interface ISecurityAbility {

    String SIGN_SUFFIX = "_sign_";

    List<String> getColumnsForSigning();

    List<String> getColumnsForEncryption();

    AbstractEncryptor getAEncryptor();

    default String column2SignColumn(String column) {
        return column + SIGN_SUFFIX;
    }

    /**
     * 获取因为存在签名字段所以需要追加的列
     *
     * @return 额外的签名校验列
     */
    default List<Column> getSignColumns() {
        if (getColumnsForSigning() == null) return List.of();

        return getColumnsForSigning().stream()
            .map(this::column2SignColumn)
            .map(Column::of)
            .map(c -> c.setType(ColumnType.VARCHAR))
            .toList();
    }

    /**
     * 该签名签名、该加密加密
     *
     * @param map
     */
    default void signAndEncrypt(Map map) {
        AbstractEncryptor encryptor = getAEncryptor();
        if (encryptor == null) return;

        getColumnsForSigning().forEach(s -> {
            if (map.containsKey(s)) {
                map.put(column2SignColumn(s), encryptor.sign(map.get(s).toString()));
            }
        });

        getColumnsForEncryption().forEach(s -> {
            if (map.containsKey(s)) {
                if (map.get(s) instanceof String str) {
                    map.put(s, encryptor.encrypt(str));
                } else if (map.get(s) instanceof ArrayList) {
                    ArrayList<String> list = (ArrayList<String>) map.get(s);
                    map.put(s, list.stream().map(encryptor::encrypt).toArray(String[]::new));
                } else if (map.get(s) instanceof String[] list) {
                    map.put(s, Arrays.stream(list).map(encryptor::encrypt).toArray(String[]::new));
                }
            }
        });
    }

    default void decrypt(Map map) {
        AbstractEncryptor encryptor = getAEncryptor();
        if (encryptor == null) return;

        getColumnsForEncryption().forEach(s -> {
            if (map.containsKey(s)) {
                if (map.get(s) instanceof String str) {
                    map.put(s, encryptor.decrypt(str));
                } else if (map.get(s) instanceof String[] list) {
                    map.put(s, Arrays.stream(list).map(encryptor::decrypt).toArray(String[]::new));
                }
            }
        });
    }

    default void checkSign(Map map) {
        AbstractEncryptor encryptor = getAEncryptor();
        if (encryptor == null) return;

        getColumnsForSigning().forEach(s -> {
            if (map.containsKey(s)) {
                encryptor.checkSign(map.get(s).toString(), map.get(column2SignColumn(s)).toString());
            }
        });
    }

}
