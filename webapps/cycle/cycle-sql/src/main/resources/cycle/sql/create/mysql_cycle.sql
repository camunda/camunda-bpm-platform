
    create table cy_bpmn_diagram (
        id bigint not null,
        connectorId bigint,
        diagramPath varchar(255),
        label varchar(255),
        lastModified datetime,
        lastSync datetime,
        modeler varchar(255),
        status varchar(255),
        primary key (id)
    ) ENGINE=InnoDB
;

    create table cy_connector_attributes (
        configuration_id bigint not null,
        value varchar(255),
        name varchar(255) not null,
        primary key (configuration_id, name)
    ) ENGINE=InnoDB
;

    create table cy_connector_config (
        id bigint not null,
        connectorClass varchar(255),
        connectorName varchar(255),
        globalPassword varchar(255),
        globalUser varchar(255),
        loginMode varchar(255),
        name varchar(255),
        primary key (id)
    ) ENGINE=InnoDB
;

    create table cy_connector_cred (
        id bigint not null,
        password varchar(255),
        username varchar(255),
        connectorConfiguration_id bigint,
        user_id bigint,
        primary key (id)
    ) ENGINE=InnoDB
;

    create table cy_roundtrip (
        id bigint not null,
        lastSync datetime,
        lastSyncMode varchar(255),
        name varchar(255),
        leftHandSide_id bigint,
        rightHandSide_id bigint,
        primary key (id)
    ) ENGINE=InnoDB
;

    create table cy_user (
        id bigint not null,
        admin boolean not null,
        email varchar(255),
        name varchar(255),
        password varchar(255),
        primary key (id)
    ) ENGINE=InnoDB
;

    alter table cy_connector_attributes 
        add index CY_FK_ATTR_CONNECTOR_CONFIG_ (configuration_id),
        add constraint CY_FK_ATTR_CONNECTOR_CONFIG_
        foreign key (configuration_id) 
        references cy_connector_config (id)
;

    alter table cy_connector_cred 
        add index CY_FK_CRED_CONNECTOR_CONFIG_ (connectorConfiguration_id),
        add constraint CY_FK_CRED_CONNECTOR_CONFIG_
        foreign key (connectorConfiguration_id) 
        references cy_connector_config (id)
;

    alter table cy_connector_cred 
        add index CY_FK_CRED_USER_ (user_id),
        add constraint CY_FK_CRED_USER_
        foreign key (user_id) 
        references cy_user (id)
;

    alter table cy_roundtrip 
        add constraint UK_5vmeky0jltxkrrymim068yuog unique (name)
;

    alter table cy_roundtrip 
        add index CY_FK_ROUNDTRIP_DIAGRAM_LHS_ (leftHandSide_id),
        add constraint CY_FK_ROUNDTRIP_DIAGRAM_LHS_
        foreign key (leftHandSide_id) 
        references cy_bpmn_diagram (id)
;

    alter table cy_roundtrip 
        add index CY_FK_ROUNDTRIP_DIAGRAM_RHS_ (rightHandSide_id),
        add constraint CY_FK_ROUNDTRIP_DIAGRAM_RHS_
        foreign key (rightHandSide_id) 
        references cy_bpmn_diagram (id)
;

    create table cy_id_table (
         tablename varchar(255),
         id integer 
    ) 
;
