package net.ximatai.muyun.model;

import io.vertx.core.json.JsonObject;

public interface IRuntimeUser {

    IRuntimeUser WHITE = new IRuntimeUser() {
        @Override
        public String getId() {
            return "0";
        }

        @Override
        public String getName() {
            return "白名单用户";
        }

        @Override
        public String getUsername() {
            return "white";
        }

        @Override
        public String getOrganizationId() {
            return "0";
        }

        @Override
        public String getDepartmentId() {
            return "0";
        }
    };

    String getId();

    String getName();

    String getUsername();

    String getOrganizationId();

    String getDepartmentId();

    static IRuntimeUser build(String id) {
        return new IRuntimeUser() {

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getUsername() {
                return "";
            }

            @Override
            public String getOrganizationId() {
                return "";
            }

            @Override
            public String getDepartmentId() {
                return "";
            }
        };
    }

    static IRuntimeUser build(JsonObject object) {
        return new IRuntimeUser() {

            @Override
            public String getId() {
                return object.getString("id");
            }

            @Override
            public String getName() {
                return object.getString("name");
            }

            @Override
            public String getUsername() {
                return object.getString("username");
            }

            @Override
            public String getOrganizationId() {
                return object.getString("organizationId");
            }

            @Override
            public String getDepartmentId() {
                return object.getString("departmentId");
            }
        };
    }
}
