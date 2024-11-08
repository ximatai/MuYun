package net.ximatai.muyun.platform.controller;

import io.quarkus.scheduler.Scheduled;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.platform.model.OnlineDevice;
import net.ximatai.muyun.platform.model.OnlineUser;
import net.ximatai.muyun.util.UserAgentParser;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.OnlineController.MODULE_ALIAS;

@Tag(name = "在线用户")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class OnlineController implements IRuntimeAbility {
    public final static String MODULE_ALIAS = "online";

    @Inject
    EventBus eventBus;

    @Inject
    RoutingContext routingContext;

    @Inject
    DepartmentController departmentController;

    @Inject
    OrganizationController organizationController;

    private Set<OnlineUser> onlineUsers = new HashSet<>();
    private List<OnlineDevice> onlineDevices = new ArrayList<>();

    @PostConstruct
    void init() {
        eventBus.<String>consumer("web.online.pong", msg -> {
            String deviceID = msg.body();
            Optional<OnlineDevice> device = onlineDevices.stream().filter(it -> it.getId().equals(deviceID)).findFirst();
            device.ifPresent(onlineDevice -> onlineDevice.setLastActiveTime(LocalDateTime.now()));
        });
    }

    @GET
    @Path("/view")
    public List<OnlineUser> view() {
        return onlineUsers.stream().sorted(Comparator.comparing(OnlineUser::getCheckInTime)).toList();
    }

    @GET
    @Path("/iAmHere")
    public String iAmHere() {
        String deviceID = UUID.randomUUID().toString();

        IRuntimeUser user = getUser();

        LocalDateTime now = LocalDateTime.now();

        String userAgent = routingContext.request().getHeader("User-Agent");
        String os = UserAgentParser.getOS(userAgent);
        String browser = UserAgentParser.getBrowser(userAgent);

        OnlineDevice onlineDevice = new OnlineDevice()
            .setId(deviceID)
            .setOs(os)
            .setBrowser(browser)
            .setActive(true)
            .setCheckInTime(now)
            .setLastActiveTime(now);

        onlineDevices.add(onlineDevice);
        OnlineUser onlineUser = getOnlineUserByID(user.getId());

        if (onlineUser == null) {
            onlineUser = new OnlineUser()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setName(user.getName())
                .setDepartmentId(user.getDepartmentId())
                .setOrganizationId(user.getOrganizationId())
                .setDepartmentName(departmentController.idToName(user.getDepartmentId()))
                .setOrganizationName(organizationController.idToName(user.getOrganizationId()));

            onlineUsers.add(onlineUser);
        }

        onlineDevice.setOnlineUser(onlineUser);
        onlineUser.getDeviceList().add(onlineDevice);

        return deviceID;
    }

    private OnlineUser getOnlineUserByID(String id) {
        for (OnlineUser user : onlineUsers) {
            if (id.equals(user.getId())) {
                return user;
            }
        }

        return null;
    }

    @Scheduled(every = "10s")
    public void executeTask() {
        eventBus.publish("web.online.ping", true);

        // 定时是10s的，所以20s没有反馈的就是下线了的
        LocalDateTime thresholdTime = LocalDateTime.now().minusSeconds(20);

        List<OnlineDevice> toRemoveDevice = onlineDevices.stream()
            .filter(it -> it.getLastActiveTime().isBefore(thresholdTime))
            .toList();

        toRemoveDevice.forEach(onlineDevice -> {
            onlineDevice.getOnlineUser().getDeviceList().remove(onlineDevice);
            onlineDevices.remove(onlineDevice);
        });

        // 一个设备都不在线的用户应该删除
        List<OnlineUser> toRemoveUser = onlineUsers.stream()
            .filter(it -> it.getDeviceList().isEmpty())
            .toList();

        toRemoveUser.forEach(onlineUser -> {
            onlineUsers.remove(onlineUser);
        });
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }

}

