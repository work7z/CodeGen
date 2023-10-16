package com.denote.client.core


import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils

class InitializerLogicFunc extends BasicLogicFunc {
    static def idColumn = "id bigint PRIMARY KEY auto_increment NOT NULL";
    static def createTimeColumn = " create_time timestamp DEFAULT current_timestamp ";

    @Override
    APIResponse handle(APIRequest apiRequest) {
        def updateColumn = " update_time timestamp ";
        def g = GData.g();

        GData.g().queryFirst("select 1", [:])

//        def shouldRemove = true;
        def shouldRemove = false;
        if (shouldRemove && GUtils.isDevMode()) {
            ["g_static_config", "g_proxy_folder", "g_static_config", "g_static_folder"
             , "g_proxy_config_http_https", "g_proxy_config"].each {
                g.exec("drop table if exists ${it}")
            }
        }

        def commonColumnForStaticServer = """
folder_id int default 0,
is_local_ssl int default 0,
file_path varchar(500),
context_path varchar(200),
local_listen_port varchar(200),
local_listen_ssl_port varchar(200),
list_directory int default 1,
plain_view_mode int default 1,
protocol varchar(30) comment 'http/https',
local_listen_ipaddr varchar(50) default '127.0.0.1',
start_running_solid_time timestamp,
brief varchar(200),
timeout int default 35,
name varchar(200),
pre_run_status int default 0,
run_status int default 0,
next_run_status int default 0,
quartz_cron_str varchar(100),
run_latest_msg varchar(20),
boot_flag int default 1,
logging_flag int default 1,
is_gzip int default 0,
view_success_info varchar(250),
view_error_info varchar(250),
"""

        def folderCommonProps = """
${idColumn},
name varchar(80) not null,
brief varchar(280),
is_remark int default 0,
${createTimeColumn}
"""
        g.exec("""
create table if not exists g_static_folder(
${folderCommonProps}
)
""").exec("""
create table if not exists g_static_config(
${idColumn},
${commonColumnForStaticServer}
${createTimeColumn}
)
""")


        // proxy server
        g.exec("""
create table if not exists g_proxy_folder(
${folderCommonProps}
);
""").exec("""
create table if not exists g_proxy_config(
    ${idColumn},
    ${commonColumnForStaticServer}
    ${createTimeColumn}
);
""").exec("""
create table if not exists g_proxy_config_rule(
    ${idColumn},
    config_id int not null,
    rule_name varchar(100),
    rule_brief varchar(250),
    dest_host varchar(100) not null,
    read_timeout int default -1,
    connect_timeout int default -1,
    max_connection int default -1,
    handle_compress int default 1,
    handle_redirect int default 1,
    keep_cookies int default 0,
    is_change_origin int default 0 comment 'keep_host',
    send_url_fragment int default 1,
    forward_ip int default 1,
    use_system_properties int default 1,
    
    disable int default 0,
    ${createTimeColumn}
);
""").exec("""
create table if not exists g_proxy_config_rules_path_rewrite(
${idColumn},
config_rule_id int not null,
from_url_pattern varchar(150),
to_url_pattern varchar(150),
disable int default 0,
${createTimeColumn}
)
""");

        // system logging information
        g.exec("""
create table if not exists g_infra_boot_record(
    ${idColumn},
    puid varchar(38) not null,
    version varchar(50),
    last_active_time timestamp,
    ${createTimeColumn}
)
""").exec("""
create table if not exists g_infra_logging(
    ${idColumn},
    log_type int default 0, 
    msg_source varchar(20),
    msg_content text,
    thread_name varchar(100),
    thread_id varchar(100),
    puid varchar(38),
    ${createTimeColumn}
)
""")


        // system settings
        g.exec("""
create table if not exists g_sys_setting(
    ${idColumn},
    mykey varchar(50),
    myvalue varchar(200),
    fact_value varchar(200),
    ${createTimeColumn}
);
""").exec("""
create table if not exists g_sys_shortcut(
    ${idColumn},
    is_ctrl_down int default 0,
    is_shift_down int default 0,
    is_alt_down int default 0,
    keycode varchar(30),
    ${createTimeColumn}
);
""").exec("""
create table if not exists g_sys_user_cache(
    ${idColumn},
    user_id varchar(32),
    user_name varchar(50),
    pin_code varchar(10),
    ${createTimeColumn}
)
""")

        // messages channel
        GData.g().exec("""
create table if not exists g_message_channel(
    ${idColumn},
    intent_type varchar(20),
    opt_type varchar(100),
    title varchar(100),
    text_content varchar(500),
    json_content json,
    has_read int default 0, 
    error_info varchar(100),
    invoke_method varchar(100),
    ${createTimeColumn}
)
""")

        // system info defined
        g.exec("""
create table if not exists g_sys_boot_record(
    ${idColumn},
    boot_initial_time timestamp,
    last_active_time timestamp,
    ${createTimeColumn}
)
""")
        g.exec("""
create table if not exists g_sys_missile_dispatch_record(
    ${idColumn},
    boot_id int not null,
    dispatch_type varchar(50) not null,
    dispatch_name varchar(200) not null,
    dispatch_status varchar(20),
    has_error int default 0,
    has_done int default 0,
    has_stop int default 0,
    ${createTimeColumn}
)
""")
        // server static logging
        g.exec("""
create table if not exists g_static_server_run_log(
${idColumn},
config_id int not null,
dispatch_record_id int not null,
http_method varchar(50),
is_http_ssl int default 0,
req_url varchar(20) not null,
res_http_code varchar(10),
mapping_file_path text,
file_length int default 0,
transmit_spent_time int default 0,
is_match_file int default 0,
${createTimeColumn}
)
""")
        // SQL connections

        g.exec("""
create table if not exists g_dblink_connections_folder(
${idColumn},
folder_name varchar(100) not null,
folder_brief varchar(500) null,
children_folder_id varchar(800),
parent_folder_id int null,
update_version varchar(32),
${createTimeColumn}
)
""").execSafe("""
alter table g_dblink_connections_folder add column is_expand int default 1,
""").exec("""
create table if not exists g_dblink_connections(
${idColumn},
is_connection int default 0,
folder_id int default 0,
dbtype_id int not null,
driver_id int not null,
connection_name varchar(100),
connection_brief varchar(500) null,
jdbc_url varchar(500),
username varchar(100),
host varchar(100),
connection_type varchar(30),
password varchar(200),
connection_type varchar(30),
port varchar(100),
default_database varchar(100),
save_password_locally int default 0,
update_version varchar(32),
${createTimeColumn}
)
""").exec("""
create table if not exists g_dblink_connections_scripts(
${idColumn},
conn_id int not null,
activity_id int default 0,
transaction_isolation int,
editor_name varchar(100),
data_file_path varchar(200),
temp_file_path varchar(200),
has_drafts int default 0,
${createTimeColumn}
)
""").exec("""
create table if not exists g_dblink_dbtype(
${idColumn},
database_name varchar(100),
database_icon varchar(100),
database_prop varchar(100),
${createTimeColumn}
)
""").exec("""
create table if not exists g_dblink_driver(
${idColumn},
dbtype_id int not null,
driver_name varchar(100),
mvn_group_id varchar(100),
mvn_artifact_id varchar(100),
mvn_version varchar(100),
local_jar_file varchar(500),
system_file int default 1,
${createTimeColumn}
)
""")


        // initializing config
        ["g_static_folder", "g_proxy_folder"].each {
            def ctn = GData.g().ctn("select * from ${it}")
            if (ctn == 0) {
                GData.modify(it, "insert", [
                        "name" : "All Groups",
                        "brief": "Created by System"
                ])
            }
        }

        // setting up
        GlobalController.call(SettingDbLinkBasisLogicFunc.class, apiRequest)

        // run settings start up checking
        GlobalController.call(SettingStartUpCheckingLogicFunc.class, apiRequest)

        // clean when start-up
        GlobalController.callOnce(VacuumCleanerAndStartUpLogicFunc.class, apiRequest)

        return null;
    }

    static void main(String[] args) {
        def rawstr = """
create table if not exists g_temp_test(
    ${idColumn},
    boot_id int not null,
    dispatch_type varchar(50) not null,
    dispatch_name varchar(200) not null,
    dispatch_status varchar(20),
    has_error int default 0,
    error_flag int default 0,
    has_done int default 0,
    has_stop int default 0,
    ${createTimeColumn}
)
"""
        GData.g().exec(rawstr)
    }
}
