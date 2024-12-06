package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.controller.NoticeController;
import net.ximatai.muyun.platform.controller.ReceiveController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestNoticeController {
    
    @Inject
    MuYunConfig config;
    
    @Inject
    NoticeController noticeController;
    
    @Inject
    ReceiveController receiveController;
    
    @Test
    public void publish() {
        String id = noticeController.create(Map.of(
            "v_title", "Chinese table tenni",
            "v_context", "Malone fades from center stage",
            "dict_notice_access_scope", "open"
        ));
        int release = noticeController.release(id);
        PageResult view = receiveController.view(10, 10L, true, null);
        List<Map> list = view.getList();
        Optional optional = list.stream().filter(ele -> ele.get("id").equals(id)).findFirst();
        assertTrue(optional.isPresent());
    }
}
