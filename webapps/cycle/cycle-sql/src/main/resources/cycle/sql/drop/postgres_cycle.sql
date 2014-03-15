
    alter table cy_connector_attributes 
        drop constraint CY_FK_ATTR_CONNECTOR_CONFIG_
;

    alter table cy_connector_cred 
        drop constraint CY_FK_CRED_CONNECTOR_CONFIG_
;

    alter table cy_connector_cred 
        drop constraint CY_FK_CRED_USER_
;

    alter table cy_roundtrip 
        drop constraint CY_FK_ROUNDTRIP_DIAGRAM_LHS_
;

    alter table cy_roundtrip 
        drop constraint CY_FK_ROUNDTRIP_DIAGRAM_RHS_
;

    drop table if exists cy_bpmn_diagram cascade
;

    drop table if exists cy_connector_attributes cascade
;

    drop table if exists cy_connector_config cascade
;

    drop table if exists cy_connector_cred cascade
;

    drop table if exists cy_roundtrip cascade
;

    drop table if exists cy_user cascade
;

    drop table if exists cy_id_table cascade
;
