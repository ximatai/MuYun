package net.ximatai.muyun.model;

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

}
