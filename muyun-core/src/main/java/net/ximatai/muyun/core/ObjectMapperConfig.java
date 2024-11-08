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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@ApplicationScoped
public class ObjectMapperConfig {

    @Produces
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建一个自定义的 SimpleModule 并注册自定义的序列化器和反序列化器
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(Date.class, new CustomDateSerializer());
        customModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        customModule.addSerializer(Duration.class, new CustomDurationSerializer());

        objectMapper.registerModule(customModule);
        return objectMapper;
    }
}

// 自定义 Date 序列化器
class CustomDateSerializer extends StdSerializer<Date> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

// 自定义 LocalDateTime 序列化器
class CustomLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CustomLocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.format(FORMATTER));
    }
}

// 自定义 Duration 序列化器
class CustomDurationSerializer extends StdSerializer<Duration> {

    public CustomDurationSerializer() {
        super(Duration.class);
    }

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.getSeconds());
    }
}

