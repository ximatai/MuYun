package net.ximatai.muyun.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@ApplicationScoped
public class ObjectMapperConfig {

    @Produces
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建一个自定义的 SimpleModule 并注册自定义的 Date 序列化器
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(Date.class, new CustomDateSerializer());

        // 将模块注册到 ObjectMapper 中
        objectMapper.registerModule(dateModule);
        return objectMapper;
    }

}

class CustomDateSerializer extends StdSerializer<Date> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区
    }

    public CustomDateSerializer() {
        this(null);
    }

    public CustomDateSerializer(Class<Date> t) {
        super(t);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String formattedDate = DATE_FORMAT.format(value);
        gen.writeString(formattedDate);
    }
}
