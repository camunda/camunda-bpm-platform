
    alter table cy_connector_attributes 
        drop 
        foreign key CY_FK_ATTR_CONNECTOR_CONFIG_
;

    alter table cy_connector_cred 
        drop 
        foreign key CY_FK_CRED_CONNECTOR_CONFIG_
;

    alter table cy_connector_cred 
        drop 
        foreign key CY_FK_CRED_USER_
;

    alter table cy_roundtrip 
        drop 
        foreign key CY_FK_ROUNDTRIP_DIAGRAM_LHS_
;

    alter table cy_roundtrip 
        drop 
        foreign key CY_FK_ROUNDTRIP_DIAGRAM_RHS_
;

    drop table if exists cy_bpmn_diagram
;

    drop table if exists cy_connector_attributes
;

    drop table if exists cy_connector_config
;

    drop table if exists cy_connector_cred
;

    drop table if exists cy_roundtrip
;

    drop table if exists cy_user
;

    drop table if exists cy_id_table
;
