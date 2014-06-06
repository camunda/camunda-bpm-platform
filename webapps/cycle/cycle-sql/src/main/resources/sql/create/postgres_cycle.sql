
    create table cy_bpmn_diagram (
        id int8 not null,
        connectorId int8,
        diagramPath varchar(255),
        label varchar(255),
        lastModified timestamp,
        lastSync timestamp,
        modeler varchar(255),
        status varchar(255),
        primary key (id)
    )
;

    create table cy_connector_attributes (
        configuration_id int8 not null,
        value varchar(255),
        name varchar(255) not null,
        primary key (configuration_id, name)
    )
;

    create table cy_connector_config (
        id int8 not null,
        connectorClass varchar(255),
        connectorName varchar(255),
        globalPassword varchar(255),
        globalUser varchar(255),
        loginMode varchar(255),
        name varchar(255),
        primary key (id)
    )
;

    create table cy_connector_cred (
        id int8 not null,
        password varchar(255),
        username varchar(255),
        connectorConfiguration_id int8,
        user_id int8,
        primary key (id)
    )
;

    create table cy_roundtrip (
        id int8 not null,
        lastSync timestamp,
        lastSyncMode varchar(255),
        name varchar(255),
        leftHandSide_id int8,
        rightHandSide_id int8,
        primary key (id)
    )
;

    create table cy_user (
        id int8 not null,
        admin boolean not null,
        email varchar(255),
        name varchar(255),
        password varchar(255),
        primary key (id)
    )
;

    alter table cy_connector_attributes 
        add constraint CY_FK_ATTR_CONNECTOR_CONFIG_
        foreign key (configuration_id) 
        references cy_connector_config
;

    alter table cy_connector_cred 
        add constraint CY_FK_CRED_CONNECTOR_CONFIG_
        foreign key (connectorConfiguration_id) 
        references cy_connector_config
;

    alter table cy_connector_cred 
        add constraint CY_FK_CRED_USER_
        foreign key (user_id) 
        references cy_user
;

    alter table cy_roundtrip 
        add constraint UK_5vmeky0jltxkrrymim068yuog unique (name)
;

    alter table cy_roundtrip 
        add constraint CY_FK_ROUNDTRIP_DIAGRAM_LHS_
        foreign key (leftHandSide_id) 
        references cy_bpmn_diagram
;

    alter table cy_roundtrip 
        add constraint CY_FK_ROUNDTRIP_DIAGRAM_RHS_
        foreign key (rightHandSide_id) 
        references cy_bpmn_diagram
;

    create table cy_id_table (
         tablename varchar(255),
         id int4 
    ) w
;
