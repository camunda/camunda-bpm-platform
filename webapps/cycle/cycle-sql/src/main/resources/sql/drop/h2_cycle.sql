
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

    drop table cy_bpmn_diagram if exists
;

    drop table cy_connector_attributes if exists
;

    drop table cy_connector_config if exists
;

    drop table cy_connector_cred if exists
;

    drop table cy_roundtrip if exists
;

    drop table cy_user if exists
;

    drop table cy_id_table if exists
;
