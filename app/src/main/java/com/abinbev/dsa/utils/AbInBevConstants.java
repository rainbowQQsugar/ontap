package com.abinbev.dsa.utils;

import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

/**
 * @author Jason Harris (jason@akta.com)
 */
public interface AbInBevConstants extends DSAConstants {

    String ID = "Id";

    String BOOLEAN_YES = "Sí";
    String BOOLEAN_NO = "No";

    String SF_CREATED_DATE = "CreatedDate";

    String PROMOTION_RECORD_TYPE = "Promotion";

    String RECORD_TYPE_ID = "RecordTypeId";

    interface AccountRecordType {
        String ACCOUNT = "Account";
        String PROSPECT = "Prospect";
    }

    interface AccountType {
        String COMPETITOR = "Competitor";
    }

    interface SurveyType {
        String QUIZ = "Quiz";
    }

    interface NegotiationType {
        String PROMOTION = "Promotion";
        String SELL_IN = "Product Sell-In";
        String CONTRACT = "PO Contract";
        String TRADE_PROGRAM = "Trade Program";
        String LISTING = "Listing";
        String SALES_EVENT = "Sales Event";
    }

    interface DynamicFetch {
        String ACCOUNT_CHECKED_IN = "accountCheckedIn";
        String MORNING_MEETING_CHECKED_IN = "morningMeetingCheckedIn";
        String SPR_LIST_OPENED = "sprListData";
        String TIMELINE_OPENED = "timelineEvents";
        String TRADE_PROGRAM_OPENED = "tradeProgramListData";
        String CHECK_LATEST_VERSION = "checkLatestVersion";
        String NOTIFICATION_MESSAGE_LATEST = "notificationMessageLatest";
        String NOTIFICATION_USER_READ = "notificationUserRead";
    }

    interface AbInBevObjects extends DSAObjects {
        String ASSET_ACTIONS = "AssetActions__c";
        String ESTANDAR = "Estandar__c";
        String FLEXIBLE_DATA = "Dato_flexible__c";
        String CASOS = "Case_Force__c";
        String CASE = "Case_Force__c";
        String RECORD_TYPE = "RecordType";
        String TASK = "Task";
        String SURVEY_TAKER = "SurveyTaker__c";
        String SURVEY_C = "Survey__c";
        String PROMOTIONS = "Promociones__c";
        String ACCOUNT_ASSET_C = "Account_Asset__c";
        String ACCOUNT_ASSET_TRACKING_C = "AccountAssetTracking__c";
        String NEGOTIATIONS = "Negotiation__c";
        String PRODUCT_NEGOTIATIONS = "CN_Product_Negotiation__c";
        String NOTE = "Note";
        String PEDIDO = "Order__c";
        String ORDER_ITEM = "Order_Item__c";
        String PACKAGE_ITEM = "Item_por_Paquete__c";
        String MATERIAL_GIVE = "Material_Give__c";
        String MATERIAL_GET = "Material_Get__c";
        String PACKAGE = "Paquetes_por_segmento__c";
        String ORDER_TYPE_PRODUCT_TYPE_MAPPING = "OrderType_ProductType_Mapping__c";
        String PARAMETRO = "Parametro__c";
        String CASE_COMMENTS = "Case_Force_Comment__c";
        String NEGOTIATION_ITEM = "Negotiation_Item__c";
        String RESULT_SCALE = "Resultado_por_escala__c";
        String CASESTATUSLIST = "CaseStatusList__c";
        String SURVEY_Question_Response = "SurveyQuestionResponse__c";
        String NEGOTIATION_LIMIT = "Negotiation_Limit__c";
        String SURVEY_Question = "Survey_Question__c";
        String PERMISSIONS = "OnTap_Permission__c";
        String PROFILE = "Profile";
        String MARKET_PROGRAM = "Market_Program__c";
        String MARKET_PROGRAM_ITEM = "Market_Program_Item__c";
        String PRODUCT = "Product__c";
        String EVENT_CATALOG = "Event_Catalog__c";
        String EVENT_EQUIPMENT = "Event_Equipment__c";
        String KPI = "KPI__c";
        String RESOURCE = "Resource__c";
        String SENSITIVE_DATA = "SensitiveData__c";
        String MORNING_MEETING = "Morning_Meeting2__c";
        String MEETING_ATTENDEE = "Meeting_Attendee2__c";
        String OFFICE_LOCATION = "Office_Location2__c";
        String CN_SPR = "CN_SPR__c";
        String MANDATORY_TASK_GROUP = "Mandatory_Task_Group__c";
        String MANDATORY_TASK_DETAIL = "Mandatory_Task_Detail__c";
        String PBO_CONTRACT_TO_ACCOUNT = "CN_PBO_ContractToAccount__c";
        String PBO_CONTRACT = "CN_PBO_Contract__c";
        String PBO_CONTRACT_ITEM = "CN_PBO_Contract_Item__c";
        String CN_DISTRIBUTION = "CN_Distribution__c";
        String CN_TECHNICIANS = "CN_Technicians__c";
        String CN_ACCOUNT_TECHNICIANS = "CN_Account_Technicians__c";
        String PRICE_COLLECTION_HEADER = "CN_Price_Collection_Header__c";
        String PICTURE_AUDIT_STATUS = "Picture_Audit_Status__c";
        String CN_Notification_Message = "CN_Notification_Message__c";
        String Version_Controller = "CN_App_Version__c";
        String CN_NotificationUserRead = "CN_NotificationUserRead__c";
        String CN_DSA_Azure_File__c = "CN_DSA_Azure_File__c";
        String CN_DSA_Azure_File_Usage__c = "CN_DSA_Azure_File_Usage__c";
        String CN_KPI_Dict__c = "CN_KPI_Dict__c";
        String CN_KPI_Statistic__c = "CN_KPI_Statistic__c";
        String CN_SKU_TC_Relationship__c = "CN_SKU_TC_Relationship__c";
    }

    interface AssetActionFields {
        String ACTION_AUDIT_CASE = "Auditar";
        String ACTION_ASSET_CASE = "Asset_Case";
        String ACTION = "Action__c";
        String RECORD_TYPE = "RecordType__c";
        String ACTION_LABEL = "Action_Label__c";
        String COUNTRY_CODE = "CountryCode__c";
    }

    interface AccountFields extends DSAConstants.AccountFields {
        String LATITUDE__C = "Latitude__c";
        String LONGITUDE__C = "Longitude__c";
        String CAN_SUBCANAL_LO__C = "Can_subcanal_lo__c";
        String CAN_SUBCANAL_LO__R = "Can_subcanal_lo__r";
        String CAN_SUBCANAL_REG__C = "Can_subcanal_reg__c";
        String CAN_SUBCANAL_REG__R = "Can_subcanal_reg__r";
        String OWNER_ID = "OwnerId";
        String PARENT_ID = "ParentId";
        String PROXIMA_VISITA_BD_C = "Proxima_visita_BD__c";
        String PROXIMA_LLAMADA_TV__C = "Proxima_llamada_TV__c";
        String CI_VOL_VENTAS__C = "Ci_vol_ventas__c";
        String CI_VOL_VENTAS__R = "Ci_vol_ventas__r";
        String CIA_MEDA_SECUN__R = "Cia_meda_secun__r";
        String CIA_MEDA_SECUN__C = "Cia_meda_secun__c";
        String TIPO_PARAMETRO__C = "Tipo_parametro__c";
        String INDICADOR_TV_O_PV__C = "Indicador_TV_o_PV__c";
        String CREDITO_DISPONIBLE = "Credito_disponible__c";
        String LIMITE_CREDITO = "Limite_de_credito__c";
        String CODIGO_DEL_CLIENTE__C = "Codigo_del_cliente__c";
        String SAP_NUMBER__C = "SAP_Number__c";
        String SEGMENTO_NEGOCIACION__C = "Segmento_negociacion__c";
        String PHONE_OTHER = "Otros_telefonos__c";
        String CENTRO_DISTRIBUCION__C = "Centro_distribucion__c";
        String ULTIMA_VISITA__C = "Ultima_Visita__c";
        String ZONA_BD__C = "Zona_bd__c";
        String DESCRIPTION = "Description";
        String STREET_NUMBER__C = "Street_Number__c";
        String STREET__C = "Street__c";
        String NEIGHBORHOOD__C = "Neighborhood__c";
        String CITY__C = "CityName__c";
        String COLONY__C = "Colony__c";
        String MUNICIPALITY__C = "Municipality__c";
        String PROVINCE_C = "Province__c";
        String PREFERENCE__C = "Preference__c";
        String CLASSIFICATION__C = "Classification__c";
        String BUSINESS_UNIT__C = "Business_Unit__c";
        String SALES_OFFICE__C = "Sales_Office__c";
        String CONTACT_FIRST_NAME__C = "Contact_First_Name__c";
        String CONTACT_LAST_NAME__C = "Contact_Last_Name__c";
        String MAIN_PHONE__C = "Main_Phone__c";
        String SECONDARY_PHONE__C = "Secondary_Phone__c";
        String MOBILE_PHONE__C = "Mobile_Phone__c";
        String EMAIL__C = "Email__c";
        String PREFERRED_SERVICE_DAYS__C = "Preferred_Service_Days__c";
        String PREFERRED_SERVICE_HOURS__C = "Preferred_Service_Hours__c";
        String SEGMENT__C = "Segment__c";
        String VOLUME_CURRENT__C = "Volume_Current__c";
        String PREFERRED_SIZE__C = "Preferred_Size__c";
        String ECONOMIC_ACTIVITY__C = "Economic_Activity__c";
        String LICENSE__C = "License__c";
        String BUSINESS_FORMAT__C = "Business_Format__c";
        String POSTAL_CODE__C = "PostalCode__c";
        String LOAN_AMOUNT__C = "Loan_Amount__c";
        String LOAN_END_DATE__C = "Loan_End_Date__c";
        String CREDIT_AMOUNT__C = "Credit_Amount__c";
        String CREDIT_TERM_LENGTH__C = "Credit_Term_Length__c";
        String COOLER__C = "Cooler__c";
        String REFRIGERATOR__C = "Refrigerator__c";
        String FACADE_BRANDING__C = "Facade_Branding__c";
        String NUMBER_OF_MATERIAL__C = "Number_of_Material__c";
        String LOAN__C = "Loan__c";
        String RECORD_TYPE_ID = "RecordTypeId";
        String NUMBER_OF_REFRIGERATOR_DOORS__C = "Refrigeration_doors__c";
        String NOT_INTERESTED__C = "Not_Interested__c";
        String LOCAL_OWNER__C = "Local_Owner__c";
        String NEGOTIATION_STATUS__C = "Negotiation_Status__c";
        String CATEGORY = "Category__c";
        String CHANNEL = "Channel__c";
        String CITY_REGION = "City_Region__c";
        String PROSPECT_STATUS = "CN_Prospect_Status__c";
        String OWNER_ASSIGNED_DATE = "CN_Owner_Assigned_Date__c";
        String POC_TYPE = "CN_POC_Type__c";
        String POC_CLOSURE_REQUEST = "CN_POC_Closure_Request__c";
        String ACCOUNT_STATUS = "Account_Status__c";
        String VISIT_PERIOD = "CN_Visit_Period__c";
        String TYPE = "Type";
        String CN_LEAD_SOURCE__C = "CN_Lead_Source__c";
        String D1_TECHNICIAN__C = "CN_D1_Technician__c";
        String Is_B2B_Registered__c = "CN_Is_B2B_Registered__c";
        String B2B_Code__c = "CN_B2B_Code__c";
    }

    interface AccountStatus {
        String BLOCKED = "Blocked";
        String INACTIVE_CLIENT = "Inactive Client";
    }

    interface ProspectStatus {
//        String NOT_VISITED = "Not visited";
//        String PENDING_VALIDATION = "Pending validation";
//        String DATA_VALIDATED = "Data validated";
//        String IN_NEGOTIATION = "In negotiation";
//        String NEGOTIATION_COMPLETED = "Negotiation completed";
//        String CONVERSION_SUBMITTED = "POC conversion submitted";
//        String CONVERSION_REJECTED = "POC conversion rejected";

        String OPEN = "Open";
        String CONTACTED = "Contacted";
        String SUBMITTED = "Submitted";
        String CONVERTED = "Converted";
        String REJECTED = "Rejected";
        String UNQUALIFIED = "Unqualified";
    }

    interface ProspectLeadSource {
        String BDR_SOURCE = "BDR sourced";
        String MASTER_INPUT_PUBLIC_RELATIONS = "Master Input-Public Relations";
        String MASTER_INPUT_EMPLOYEE_REFERRAL = "Master Input-Employee Referral";
        String MASTER_INPUT_INBOUNDING_CALL = "Master Input-Inbounding Call";
        String MASTER_INPUT_WEB_TO_LEAD = "Master Input-Web to Lead";
        String MASTER_INPUT_OTHERS = "Master Input-Others";
    }

    interface EventFields extends DSAConstants.EventFields {
        String STATUS_COMPLETE = "Completado";
        String STATUS_OPEN = "Abierto";
        String ESTADO_DE_VISITA__C = "Estado_de_visita__c";
        String TAREA_PROGRAMADA__C = "Tarea_programada__c";
        String DURATION_IN_MINUTES = "DurationInMinutes";
        String VISITA_CERRADA__C = "Visita_Cerrada__c";
        String OWNER_ID = "OwnerId";
        String SUBJECT = "Subject";
        String PROGRAMADA__C = "Programada__c";
        String WHAT_ID = "WhatId";
        String CONTROL_START_DATE_TIME = "Control_inicio__c";
        String CONTROL_END_DATE_TIME = "Control_fin__c";
        // CheckIn Lat/Lng fields
        String LATITUDE = "Latitud_inicio__c";
        String LONGITUDE = "Longitud_inicio__c";
        String DURATION = "Duracion_real_visita__c";

        String CATALOG = "Event_Catalog__c";
        String EQUIPMENT = "Event_Equipment__c";
        String WITH_MUSIC_BAND = "Event_with_Music_Band__c";
        String DESCRIPTION = "Description";

        String CHECKIN_EXCEPTION_NOTE = "CheckIn_Exception_Note__c";
        String CHECKOUT_EXCEPTION_NOTE = "CheckOut_Exception_Note__c";
        String CHECKOUT_LATITUDE = "CheckOut_Latitude__c";
        String CHECKOUT_LONGITUDE = "CheckOut_Longitude__c";
        String CHECKOUT_NOTE = "CheckOut_Note__c";
        String VISIT_TYPE = "CN_Visit_Type__c";

        String VISIT_CHECK_IN_DISTANCE = "CN_Visit_Check_In_Distance_From_POC__c";
        String VISIT_CHECK_OUT_DISTANCE = "CN_Visit_Check_Out_Distance_From_POC__c";
        String VISIT_LOCATION_COMPLIANCE = "CN_Visit_Location_Compliance__c";

        String MORNING_MEETING_CHECK_IN_DISTANCE = "CN_CheckIn_Distance_From_MorningMeeting__c";
        String MORNING_MEETING_CHECK_OUT_DISTANCE = "CN_CheckOut_Distance_From_MorningMeeting__c";
    }

    interface EventSubject {
        String VISIT = "Visit";
    }

    interface VisitTypes {
        String IN_PLAN = "In Plan Visit";
        String OUT_OF_PLAN = "Out of Plan Visit";
        String MORNING_MEETING = "Morning Meeting";
    }

    interface ContactFields extends DSAConstants.ContactFields {
        String ROLE = "Contact_Function__c";
        String PHONE = "Phone";
    }

    interface Estandar__c {
        String CLIENTE = "Cliente__c";
        String VARIABLE = "Variable__c";
        String IDENTIFICADOR_DOCUMENTO = "Identificador_documento__c";
        String OPORTUNIDAD = "Oportunidad__c";
        String REAL = "Real__c";
        String IDEAL = "Ideal__c";
    }

    interface FlexibleDataFields {
        String VALUE = "Valor__c";
        String TYPE = "Type__c";
        String CLIENT = "Cliente__c";
        String CONCEPTO = "Concepto__c";
        String ORDENAMIENTO_F = "Ordenamiento_F__c";
    }

    interface CasosFields {
        String CASO_DE_ACTIVOS = "Caso de Activos";
        String ACCOUNT_ID = "AccountId__c";
        String NOMBRE_DE_LA_CUENTA = "Nombre_de_la_cuenta__c";
        String STATE = "Estado__c";
        String SCHEDULED_DATE = "Fecha_programada1__c";
        String RECORD_TYPE_ID = "RecordTypeId";
        String RECORD_TYPE = "RecordType";
        String QUANTITY2__C = "Quantity2__c";
        String OWNER_ID = "OwnerId";
        String NOMBRE_DEL_CONTACTO__C = "Nombre_del_contacto__c";
        String ORIGEN_DEL_CASO__C = "Origen_del_caso__c";
        String PRIORIDAD__C = "Prioridad__c";
        String ESTADO__C = "Estado__c";
        String PARENT_ID = "ParentId__c";
        String ACTIVO_POR_CLIENTE__C = "Account_Asset__c";
        String STATUS_OPEN = "Open";
        String PARENT_ID_DEL__C = "Parent_id_del__c";

        String ASSET_C = "Asset__c";
    }

    interface CaseFields {
        String ACCOUNT_ID = "Account__c";
        String STATUS = "Status__c";
        String RECORD_TYPE_NAME = "RecordType.Name";
        String RECORD_TYPE_ID = "RecordTypeId";
        String SLA1 = "SLA1__c";
        String CONTACT_NAME = "Contact_Name__c";
        String OWNER = "OwnerId";
        String CASE_NUMBER = "CaseNumber";
        String POC_CURRENT_NAME = "CN_POC_Current_POC_Name__c";
        String POC_REJECT_REASON = "CN_POC_Reject_Reason__c";
    }

    interface CaseStatus {
        String SUBMITTED = "Submitted";
        String REJECTED = "Rejected";
    }

    interface RecordTypeFields {
        String NAME = "Name";
        String DEVELOPER_NAME = "DeveloperName";
        String OBJECT_TYPE = "SobjectType";
        String IS_ACTIVE = "IsActive";
    }

    interface RecordTypeName {
        String ACCOUNT_CHANGE_REQUEST = "Account Change Request";
    }

    interface TaskFields {
        String STATUS_OPEN = "Not Started";
        String ACCOUNT_ID = "AccountId";
        String STATUS = "Status";
        String SUBJECT = "Subject";
        String ACTIVITY_DATE = "ActivityDate";
        String WHAT_ID = "WhatId";
        String OWNER_ID = "OwnerId";
        String PRIORITY = "Priority";
        String RECORD_TYPE_ID = "RecordTypeId";
        String DESCRIPTION = "Description";
        String RESULT = "Resultado__c";
        String SCHEDULED = "Tarea_programada__c";
        String JOB_EFFECTIVE = "Tarea_Efectiva__c";
        String COMMENT = "Description";
        String TASK_RESULT = "CN_Task_Result__c";
    }

    interface KpiFields extends SyncEngineConstants.StdFields {
        String ACCOUNT_ID = "Account_id__c";
        String USER_ID = "User_id__c";
        String TARGET = "Target__c";
        String ACTUAL = "Actual__c";
        String UNIT = "Unit_of_Measure__c";
        String CATEGORY = "Categorie__c";
        String START_DATE = "Kpi_start_date__c";
        String END_DATE = "Kpi_end_date__c";
        String KPI_NAME = "KPI_name__c";
        String KPI_PARENT = "Kpi_parent_id__c";
        String KPI_NUM = "Kpi_id__c";
        String DAYS_PASSED = "Days_passed_in_period__c";
        String TOTAL_DAYS = "Total_days_in_period__c";
        String PERCENTAGE_INCREASE = "Percentage_increase_lastyear__c";
        String Percent_Completed = "Percent_Completed__c";

    }

    interface KpiCategories {
        String VOLUME = "Volume";
        String COMPENSATION = "Compensation";
        String COVERAGE = "Coverage";
    }

    interface SensitiveDataFields {
        String FIELD_NAMES = "FieldNames__c";
        String OBJECT_NAME = "ObjectName__c";
    }

    interface MorningMeetingFields extends SyncEngineConstants.StdFields {
        String WEEKDAYS = "Weekday__c";
        String OFFICE = "Office__c";
    }

    interface MeetingAttendeesFields {
        String ATTENDEE_NAME = "Attendee_Name__c";
        String MANDATORY = "Mandatory__c";
        String MORNING_MEETING = "Morning_Meeting__c";
    }

    interface OfficeLocationFields {
        String LATITUDE = "Coordinates__Latitude__s";
        String LONGITUDE = "Coordinates__Longitude__s";
    }

    interface ChSprFields extends SyncEngineConstants.StdFields {
        String ACCOUNT_ID = "Account__c";
        String NAME = "SPR_Name__c";
        String WORKING_HOUR_FROM = "Working_Hour_From__c";
        String WORKING_HOUR_TO = "Working_Hour_To__c";
        String WORK_DATE = "Work_Date__c";
        String CONTACT_PHONE = "Contact_Phone__c";
        String TEMPORARY_PROMO = "Temporary_Promo__c";
    }

    interface SurveyFields extends SyncEngineConstants.StdFields {
        String HTML_BUNDLE = "HTML5_Bundle__c";
        String RECORD_TYPE_ID = "RecordTypeId";
        String RECORD_TYPE = "RecordType";
        String PARENT = "Parent__c";
        String CHANNEL = "Channel__c";
        String CATEGORY = "Category__c";
        String CITY_REGION = "City_Region_Multiselect__c";
        String STATUS = "Status__c";
    }

    interface SurveyTakerFields {
        String STATUS_OPEN = "Abierta";
        String STATUS_COMPLETE = "Completada";
        String ACCOUNT_ID = "Account__c";
        String USER__C = "User__c";
        String STATUS = "Status__c";
        String TYPE = "Type__c";
        String SURVEY__C = "Survey__c";
        String TOTAL_SCORE = "Total_Score__c";
        String DUE_DATE = "Fecha_Programcion_Encuesta__c";
        String LAST_MODIFIED_DATE = "LastModifiedDate";
        String OWNER = "OwnerId";
    }

    interface SurveyQuestionFields {
        String PHOTO_FROM_LIBRARY = "Photo_from_Library__c";
        String QUESTION = "Question__c";
        String SURVEY = "Survey__c";
    }

    interface SurveyQuestionResponseFields {
        String SURVEY_TAKER = "SurveyTaker__c";
        String SURVEY_QUESTION = "Survey_Question__c";
    }

    interface PromotionFields {
        String CUSTOMER = "Customer__c";
        String TYPE = "Type_of_promotion__c";
        String DESCRIPTION = "Description__c";
        String START_DATE = "Starting_date__c";
        String END_DATE = "Ending_date__c";
        String OBLIGATORY = "Mandatory__c";
    }

    interface ProductNegotiationFields {
        String ACCOUNT = "Account__c";
        String PROMOTION_TYPE = "Promotion_Type__c";
        String DESCRIPTION = "Negotiation_Decription__c";
        String CATEGORY = "Promotion_Category__c";
        String STATUS = "Status__c";
        String PRODUCT = "Product__c";
        String NAME = "Name";
        String UNIT = "Unit__c";
        String BRAND = "Brand__c";
        String PTR = "Suggest_PTR__c";
        String PACKAGE = "Package__c";
        String RECORD_TYPE_ID = "RecordTypeId";
        String START_TIME = "Start_Time__c";
        String END_TIME = "End_Time__c";
    }

    interface SkuTcRelationshipFields {
        String CHANNEL = "Channel__c";
        String PRODUCT = "Product__c";
        String TERRITORY = "Territory__c";
    }

    interface PriceCollectionHeaderFields extends SyncEngineConstants.StdFields {
        String POC_ID = "POC_Id__c";
        String SURVEY_TAKEN = "Survey_Taken__c";
    }

    interface AccountAssetTrackingFields {
        String REASON = "Undetected_Reason__c";
        String COMMENT = "Undetected_Reason_Comment__c";
        String STATUS = "Asset_Status__c";
        String VISIT_ID = "Visit_ID__c";
        String PARENT_ID = "Account_Asset__c";
        String TRACKING_TIME = "CN_Last_Tracking_Time__c";
    }

    interface AccountAssetFields {
        String CLIENT = "Account__c";
        String OWNER = "OwnerId";
        String RECORD_TYPE_ID = "RecordTypeId";
        String SERIAL_NUMBER = "Serial_Number__c";
        String BRAND = "Brand__c";
        String QUANTITY = "Quantity__c";
        String STATEMENT = "Asset_Status__c";
        String WARRANTY_DATE = "Warranty_Date__c";
        String INVENTORY_DATE = "Inventory_Date__c";
        String CODE = "Asset_Code__c";
        String TYPE = "Asset_Type__c";
        String LATITUD__C = "Latitude__c";
        String LONGITUD__C = "Longitude__c";
        String DESCRIPTION__C = "Asset_Description__c";
        String QR_CODE = "QR_Code__c";
        String CN_QR_CODE = "CN_QR_Code__c";
        String CN_SUPPLEMENTARY_QR_CODE = "CN_Supplementary_QR_Code__c";
        String WIFI_TAG = "Wifi_Tag__c";
        String NAME = "Name";
        String ASSET_NAME = "Asset_Name__c";
        String REASON = "Undetected_Reason__c";
        String COMMENT = "Undetected_Reason_Comment__c";
        String CN_ASSET_CATEGORY = "Asset_Type__c";
        String CN_CLEAN_STATUS = "CN_Clean_Status__c";
        String CN_LATEST_CLEAN_TIME = "CN_Latest_Clean_Time__c";
    }

    interface NegotiationFields {
        String NEGOTIATION_ID = "Name";
        String CLIENT = "Account__c";
        String OBSERVATIONS = "Comments__c";
        String STATUS = "Status__c";
        String START_DATE = "Start_Date__c";
        String DELIVER_DATE = "End_Date__c";
        String TYPE = "Tipo_negociacion__c";
        String CLASIFICATION = "Classification__c";
        String EXPIRATION_DATE = "Fecha_fin_validez__c";
        String PESOS_PER_CASE = "Pesos_per_Case__c";
        String APPROVER = "Approver_s__c";
        String RECORD_TYPE_ID = "RecordTypeId";
    }

    interface NoteFields {
        String PARENT_ID = "ParentId";
        String TITLE = "Title";
        String BODY = "Body";
        String OWNER_ID = "OwnerId";
    }

    interface AttachmentFields extends DSAConstants.AttachmentFields {
        String LAST_MODIFIED_DATE = "LastModifiedDate";
        String NAME = "Name";
    }

    interface PedidoStatus {
        String STATUS_IN_PROGRESS = "In Progress";
        String STATUS_OPEN = "Open";
        String STATUS_SUBMITTED = "Submitted";
        String STATUS_CANCELLED = "Canceled";
        String STATUS_CONFIRM = "Confirm";
        String STATUS_IN_DELIVERY = "In Delivery";
        String STATUS_DELIVERY = "delivery";
        String STATUS_CLOSED = "Closed";
        String STATUS_REJECT = "Rejected";
        String STATUS_PROCESSING = "Processing";
        String STATUS_SHIPPED = "Shipped";
        String STATUS_RECEIVED = "Received";
        String STATUS_COMPLETED = "Completed";
    }

    interface PedidoFields {
        String CUSTOMER = "OrderAccount__c";
        String STATUS = "OrderStatus__c";
        String CREATED_DATE = "CreatedDate";
        String START_DATE = "BeginDate__c";
        String END_DATE = "EndDate__c";
        String OUT_OF_ROUTE_ORDER__C = "Out_of_Route_Order__c";
        String CASH_PICKLIST_VALUE = "0001";
        String CASH__C = "Cash__c";
        String TOTAL = "TotalAmount__c";
        String SOURCE = "CN_Order_Source__c";
        String MATERIAL_CODE_1__C = "Material_Code_1__c";
        String MATERIAL_CODE_2__C = "Material_Code_2__c";
        String MATERIAL_CODE_3__C = "Material_Code_3__c";
        String MATERIAL_CODE_4__C = "Material_Code_4__c";
        String MATERIAL_CODE_5__C = "Material_Code_5__c";
        String MATERIAL_CODE_6__C = "Material_Code_6__c";
        String MATERIAL_CODE_7__C = "Material_Code_7__c";
        String MATERIALLOOKUP_1__C = "MaterialLookup_1__c";
        String MATERIALLOOKUP_2__C = "MaterialLookup_2__c";
        String MATERIALLOOKUP_3__C = "MaterialLookup_3__c";
        String MATERIALLOOKUP_4__C = "MaterialLookup_4__c";
        String MATERIALLOOKUP_5__C = "MaterialLookup_5__c";
        String MATERIALLOOKUP_6__C = "MaterialLookup_6__c";
        String MATERIALLOOKUP_7__C = "MaterialLookup_7__c";
        String MOTIVE_1__C = "Motive_1__c";
        String MOTIVE_2__C = "Motive_2__c";
        String MOTIVE_3__C = "Motive_3__c";
        String MOTIVE_4__C = "Motive_4__c";
        String MOTIVE_5__C = "Motive_5__c";
        String MOTIVE_6__C = "Motive_6__c";
        String MOTIVE_7__C = "Motive_7__c";
        String UNIT_OF_MEASURE_1__C = "Unit_of_Measure_1__c";
        String UNIT_OF_MEASURE_2__C = "Unit_of_Measure_2__c";
        String UNIT_OF_MEASURE_3__C = "Unit_of_Measure_3__c";
        String UNIT_OF_MEASURE_4__C = "Unit_of_Measure_4__c";
        String UNIT_OF_MEASURE_5__C = "Unit_of_Measure_5__c";
        String UNIT_OF_MEASURE_6__C = "Unit_of_Measure_6__c";
        String UNIT_OF_MEASURE_7__C = "Unit_of_Measure_7__c";
        String QUANTITY_1__C = "Quantity_1__c";
        String QUANTITY_2__C = "Quantity_2__c";
        String QUANTITY_3__C = "Quantity_3__c";
        String QUANTITY_4__C = "Quantity_4__c";
        String QUANTITY_5__C = "Quantity_5__c";
        String QUANTITY_6__C = "Quantity_6__c";
        String QUANTITY_7__C = "Quantity_7__c";
        String RECORD_TYPE_ID = "RecordTypeId";
        String NEGOTIATION__C = "Negotiation__c";
        String Account__c = "OrderAccount__c";
    }

    interface PackageFields {
        String PAQUETE__R = "Paquete__r";
        String PAQUETE__C = "Paquete__c";
        String SEGMENTO__C = "Segmento__c";
    }

    interface PackageItemFields {
        String MATERIAL_GIVE = "Material_Give__c";
        String MATERIAL_GET = "Material_Get__c";
        String PACKAGE = "Paquete__c";
        String RECORD_TYPE_ID = "RecordTypeId";
    }

    interface MaterialFields {
        String CODE = "Codigo_Material__c";
        String SCORE = "Points__c";
        String DISTRIBUTION_CENTER = "DistributionCenter__c";
        String MATERIAL_DELETION_INDICATOR = "MaterialDeletionIndicator__c";
        String RECORD_TYPE_ID = "RecordTypeId";
        String BRANDING = "Branding__c";
        String CATEGORY = "Category__c";
        String DESCRIPTION = "Descripcion__c";
        String CALCULATION = "Calculation__c";
        String POINTS = "Points__c";
        String EXCLUSIVE = "Exclusive__c";
        String GROUP = "Group_Name__c";
        String COMMENT = "Comments__c";
    }

    interface OrderTypeProductTypeMappingFields {
        String ORDER_RECORD_TYPE = "OrderRecordType__c";
        String PRODUCT_RECORD_TYPE = "ProductRecordType__c";
        String LINE_ITEM_FIELDS = "LineItemFields__c";
    }

    interface ParametroFields {
        String CODIGO = "Codigo__c";
        String COLOR = "Color__c";
    }

    interface CaseCommentFields {
        String CASE_FORCE = "Case_Force__c";
        String COMMENT = "Comment__c";
    }

    interface UserFields extends DSAConstants.UserFields {
        String COUNTRY = "Country";
        String CITY = "City__c";
        String PROFILE = "User_Profile__c";
        String PROFILE_ID = "ProfileId";
        String ZONA__C = "Zona__c";
        String EMAIL = "Email";
        String BUSINESS_UNIT = "Business_Unit__c";
        String MANAGER = "ManagerId";
        String TIME_ZONE = "TimeZoneSidKey";
        String CN_Related_KUser__c = "CN_Related_KUser__c";
        String ManagerId = "ManagerId";
        String CN_Employee_Code__c = "CN_Employee_Code__c";
    }

    interface NegotiationItemFields {
        String RECORD_TYPE_ID = "RecordTypeId";
        String AMOUNT = "Amount__c";
        String MATERIAL_GIVE = "Material_Give__c";
        String MATERIAL_GET = "Material_Get__c";
        String STATUS = "Status__c";
        String NEGOTIATION_GIVE = "Negociacion_give__c";
        String NEGOTIATION_GET = "Negociacion_get__c";
        String START_DATE = "Start_DateTime__c";
        String END_DATE = "Max_Delivery_Date__c";
        String NEGOTIATION = "Negotiation__c";
        String COMMENT = "Comments__c";
    }

    interface NegotiationLimitFields {
        String BUSINESS_UNIT = "Business_Unit__c";
        String CLASSIFICATION = "Classification__c";
        String SEGMENT = "Segment__c";
        String VOLUME_HIGH = "Volume_High__c";
        String VOLUME_LOW = "Volume_Low__c";
        String LIMIT_LOW = "Limit_Low__c";
        String LIMIT_HIGH = "Limit_High__c";
    }

    interface ResultScaleFields {
        String RESULT = "Result__c";
        String PROMOTION_ID = "Promotion__c";
    }

    interface CaseStatusListFields {
        String ESTADO1 = "Estado1__c";
        String ESTADO2 = "Estado2__c";
        String COUNTRY = "Country__c";
        String PERFILES = "Perfiles__c";
    }

    interface CN_TechniciansFields {
        String ACCOUNT = "CN_POC__c";
        String D1_NAME = "Name";
        String D1_ID = "CN_Employee_Code__c";
        String D1_PHONE = "CN_Phone__c";
    }

    interface CN_Account_TechniciansFields {
        String CN_POC = "CN_Poc__c";
        String CN_TECHNICIAN = "CN_Technician__c";
    }

    interface PermissionFields {
        String PROFILE_NAME = "Profile_Name__c";

        String VISIT_LIST = "Visit_List__c";
        String PROSPECTS = "Prospects__c";
        String ACCOUNTS = "Accounts__c";
        String QUIZZES = "Quizzes__c";
        String DSA = "DSA__c";
        String CHATTER = "Chatter__c";
        String CALCULATOR = "Calculator__c";
        String FULL_SYNC = "Full_Sync__c";

        String POCE = "POCE__c";
        String SURVEYS = "Surveys__c";
        String TASKS = "Tasks__c";
        String PROMOTIONS = "Promotions__c";
        String CASES = "Cases__c";
        String NEGOTIATIONS = "Negotiations__c";
        String ORDERS = "Orders__c";
        String ASSETS = "Assets__c";
        String MARKET_PROGRAMS = "Market_Program__c";

        String CREATE_SURVEY = "Survey_Create_New__c";
        String CREATE_NEGOTIATION = "Negotiation_Create_New__c";
        String CREATE_TASKS = "Tasks_Create_New__c";
        String CREATE_ASSETS = "Assets_Create_New__c";
        String CREATE_ORDERS = "Order_Create_New__c";
        String CREATE_CASE = "Cases_Create_New__c";

        String NEGOTIATION_GAUGE = "Negotiation_Gauge__c";

        String PROSPECT_BASIC_DATA = "Prospect_Basic_Data__c";
        String PROSPECT_ADDITIONAL_DATA = "Prospect_Additional_Data__c";
        String PROSPECT_NEGOTIATIONS = "Prospect_Negotiations__c";
        String PROSPECT_FILES = "Prospect_Files__c";

        String ASSET_PHOTO_REQUIRED = "Assets_Post_Scan_Photo_Required__c";
        String CN_ASSET_ACCOUNT360 = "CN_Asset_Account360__c";
        String USER_360_HAMBURGER_MENU = "User_360_Hamburger_Menu_Option__c";
        String ACCOUNT_KPIS = "Account_KPIs__c";
        String CN_TECHNICIANS_ACCOUNT360 = "CN_Technicians_Account360__c";
        String ACCOUNT_CHECK_IN_PICTURE_COMMENT_REQUIRED = "Account_CheckIn_Picture_Comment_Required__c";
        String MORNING_MEETING_PICTURE_COMMENT_REQUIRED = "Morning_Meeting_Picture_Comment_Required__c";
        String MINIMAL_MORNING_MEETING_HRS_INTERVAL = "Minimal_Morning_Meeting_hrs_interval__c";
    }

    interface OnTapSettingsFields extends SyncEngineConstants.StdFields {
        String CONTRACT_EXPIRATION_PERIOD = "PBO_Contract_Expiration_Warning_Period__c";
        String SETUP_OWNER_ID = "SetupOwnerId";
        String ORDER_TIMEFRAME_DAYS = "Order_Time_Frame_Days__c";
        String ORDER_TIMEFRAME_MONTHS = "Order_Time_Frame_Months__c";
        String ORDER_TIMEFRAME_VALUE = "Order_Time_Frame_Value__c";
        String CHECK_IN_DISTANCE_TRESHOLD = "checkindistance_threshold__c";
    }

    interface ProfileFields {
        String NAME = "Name";
    }

    interface OrderItemFields extends SyncEngineConstants.StdFields {
        String ORDER = "CustomerOrder__c";
        String PRODUCT = "ItemProduct__c";
        String QUANTITY = "ActualQuantity__c";
        String VOLUME_HL = "CN_Volume_HL__c ";
        String ProductDesc__c = "CN_ProductDesc__c";
        String CN_Brand__c = "CN_Brand__c";
        String B2B_ProductID__c = "CN_B2B_ProductID__c";
    }

    interface ProductFields extends SyncEngineConstants.StdFields {
        String ORDER = "Order__c";
        String PRODUCT_NAME = "MaterialProduct__c";
        String PRODUCT_SHORT_NAME = "ProductShortName__c";
        String PRODUCT_CODE = "ProductCode__c";
        String UNIT_OF_MEASURE = "UnitofMeasure__c";
        String COMPETITOR_FLAG = "Competitor_Flag__c";
        String PACKAGE = "CN_Package__c";
        String CN_BRAND = "CN_Brand__c";
        String CHANNEL = "CN_Channel__c";
        String ExternalKey__c = "ExternalKey__c";
    }

    interface MarketProgramFields extends SyncEngineConstants.StdFields {
        String ORDER = "Order__c";
        String ACCOUNT = "Account__c";
        String STATUS = "Status__c";
        String MARKET_PROGRAM = "Market_Program__c";
        String START_DATE = "Start_Date__c";
        String END_DATE = "End_Date__c";

        String CN_CONTRACT_ID = "CN_Contract_Id__c";
        String CN_TP_NAME = "CN_TP_Name__c";
    }

    interface DistributionFields extends SyncEngineConstants.StdFields {
        String LAST_COLLECTED_PTR = "Last_Collected_PTR__c";
        String LAST_COLLECTED_PTC = "Last_Collected_PTC__c";

        String CN_CATEGORY = "CN_Category__c";
        String CN_BRAND = "CN_Brand__c";
        String CN_SKU_NAME = "CN_SKU_Name__c";
        String CN_PACKAGE = "CN_Package__c";
        String CN_UNIT = "CN_Unit__c";
        String POC_ID = "POC_Id__c";
        String IS_ACTIVE = "Active_Flag__c";
        String CN_PRODUCT = "CN_Product__c";
    }

    interface PictureAuditStatusFields extends SyncEngineConstants.StdFields {
        String PARENT_EVENT = "Parent_Event__c";
        String PARENT_ID = "Parent_ID__c";
        String STATUS = "Status__c";
        String STORAGE_PICTURE_REFERENCE = "Storage_Picture_Reference__c";
        String STORED_IN = "Stored_In__c";
        String PARENT_OBJECT_NAME = "Parent_Object_Name__c";
    }

    interface DistributionCategory {
        String ABI_BRAND = "ABI Brand";
        String COMPETITION_BRAND = "Competition Brand";
    }

    interface MarketProgramItemFields extends SyncEngineConstants.StdFields {
        String MARKET_PROGRAM = "Market_Program__c";
        String TYPE = "Item_Type__c";
        String PERIOD = "Period__c";
        String VALUE = "Value__c";
        String DATE = "Date__c";
        String DESCRIPTION = "Description__c";
        String RECORD_TYPE = "RecordTypeId";

        String CN_ITEM_ID = "CN_Item_Id__c";
        String CN_ITEM_NAME = "CN_Item_Name__c";
        String CN_DESCRIPTION_ORDER = "CN_Description_Order__c";
        String CN_PROGRAM_ITEM_ID = "CN_Program_Item_ID__c";
    }

    interface EventCatalogFields extends SyncEngineConstants.StdFields {
        String STATUS = "Status__c";
        String INITIAL_DATE = "Initial_Date__c";
        String END_DATE = "End_Date__c";
        String MAX_DURATION = "Max_Duration__c"; // in hours
        String USAGE_OF_EQUIPMENT = "Usage_of_Equipment__c";
    }

    interface EventEquipmentFields extends SyncEngineConstants.StdFields {
        String STATUS = "Status__c";
        String NAME = "Name";
        String DESCRIPTION = "End_Date__c";
        String BUSINESS_UNIT = "Business_Unit__c";
        String Sales_Office__c = "Sales_Office__c";
    }

    interface ResourceFields extends SyncEngineConstants.StdFields {
        String PROFILE = "Profile_Name__c";
        String FIELD_NAME = "LabelName__c";
        String FIELD_TEXT = "LabelText__c";
    }

    interface MandatoryTaskGroupFields extends SyncEngineConstants.StdFields {
        String FIRST_MONTHLY_VISIT_ONLY = "First_Monthly_Visit_Only_Flag__c";
        String MANDATORY_TASK_GROUP_NAME = "Mandatory_Task_Group_Name__c";
        String POC_CATEGORY = "POC_Category__c";
        String POC_CHANNEL = "POC_Channel__c";
        String POC_CITY = "POC_City_Region__c";
        String USER_PROFILE = "User_Profile__c";
    }

    interface MandatoryTaskDetailFields extends SyncEngineConstants.StdFields {
        String MANDATORY_TASK_GROUP = "Mandatory_Task_Group__c";
        String ERROR_MESSAGE = "Error_Message__c";
        String MANDATORY_TASK_FIELD_NAME = "Mandatory_Task_Field_Name__c";
        String MANDATORY_TASK_OBJECT = "Mandatory_Task_Object__c";
        String SURVEY = "Survey__c";
        String TASK_TYPE = "Task_Type__c";
    }

    interface PboContractToAccountFields extends SyncEngineConstants.StdFields {
        String ACCOUNT = "Account__c";
        String PBO_CONTRACT = "CN_PBO_Contract__c";
    }

    interface PboContractItemFields extends SyncEngineConstants.StdFields {
        String PBO_CONTRACT = "CN_PBO_Contract__c";
        String PRODUCT = "Product__c";
        String SKU_ID = "SKUID__c";
        String ACTUAL = "Actual__c";
        String TARGET = "Target__c";
        String YEAR_MONTH = "Year_Month__c";
    }

    interface PboContractFields extends SyncEngineConstants.StdFields {
        String STATUS = "Contract_Status__c";
        String START_MONTH = "Start_Month__c";
        String END_MONTH = "End_Month__c";
        String SALES_MANAGER = "Sales_Manager__c";
        String CONTRACT_ID = "Contract_ID__c";
    }

    interface CNNotificationMessageFields {
        String Id = "Id";
        String Name = "Name";
        String CN_Visibility_Type__c = "CN_Visibility_Type__c";
        String CN_Category__c = "CN_Category__c";
        String CN_Title__c = "CN_Title__c";
        String CN_Description__c = "CN_Description__c";
        String CN_Due_Date__c = "CN_Due_Date__c";
        String CN_Related_ID__c = "CN_Related_ID__c";
        String CN_Notify_Time__c = "CN_Notify_Time__c";
        String OwnerID = "OwnerId";
        String IsRead = "IsRead";
        String TypeId = "TypeId";
        String ItemId = "ItemId";
        String CN_Invalid_Date__c = "CN_Invalid_Date__c";
    }

    interface CNAppVersionFields {
        String Id = "Id";
        String Name = "Name";
        String CN_RELEASE_DATE__C = "CN_Release_Date__c";
        String CN_DOWNLOAD_LINK_C = "CN_Download_Link__c";
        String CN_RELEASE_NOTES__C = "CN_Release_Notes__c";
    }

    interface CNNotificationUserReadFields {
        String CN_Notification_Message__c = "CN_Notification_Message__c";
        String User__c = "User__c";
        String Id = "Id";
    }

    interface CNDsaAzueFields {
        String ID = "Id";
        String CN_CategoryID__c = "CN_CategoryID__c";
        String File_Expire_Date__c = "CN_File_Expire_Date__c";
        String CN_Category_Mobile_Configuration__c = "CN_Category_Mobile_Configuration__c";
        String CN_File_Name__c = "CN_File_Name__c";
        String CN_File_Size__c = "CN_File_Size__c";
        String CN_File_Type__c = "CN_File_Type__c";
        String CN_User__c = "CN_User__c";
        String CN_Datetime__c = "CN_Datetime__c";
        String CN_Access_Type__c = "CN_Access_Type__c";
        String CN_DSA_Azure_File__c = "CN_DSA_Azure_File__c";
    }


    interface AuthKeysFielsd {
        String Protocol__c = "Protocol__c";
        String EndpointSuffix__c = "EndpointSuffix__c";
        String ChinaBackend_Auth_API_URI__c = "CN_ChinaBackend_Auth_API_URI__c";
        String AccountKey__c = "AccountKey__c";
        String ChinaBackend_Auth_Method_Name__c = "CN_ChinaBackend_Auth_Method_Name__c";
        String CN_ChinaBackend_Post_Visit_API_URI__c = "CN_ChinaBackend_Post_Visit_API_URI__c";
    }

    interface B2BOrderFields {
        String Format = "format";
        String Method = "method";
        String ABI_CODE = "abi_code";
        String SIGN = "sign";
    }

    interface KPIStatisticFields {
        String CN_TTL_Volume__c = "CN_TTL_Volume__c";
        String CN_TTL_Volume_Target__c = "CN_TTL_Volume_Target__c";
        String CN_Key_SKU__c = "CN_Key_SKU__c";
        String CN_Key_SKU_Target__c = "CN_Key_SKU_Target__c";
        String CN_POCE_Num__c = "CN_POCE_Num__c";
        String CN_POCE_Compliance__c = "CN_POCE_Compliance__c";
        String CN_Distribution__c = "CN_Distribution__c";
        String CN_Distribution_Target__c = "CN_Distribution_Target__c";
        String CN_In_Planned_Visits__c = "CN_In_Planned_Visits__c";
        String Completed_Visits__c = "CN_Completed_Visits__c";
        String CN_B2B_Volume__c = "CN_B2B_Volume__c";
        String CN_B2B_Volume_Target__c = "CN_B2B_Volume_Target__c";
        String CN_Calculate_Date__c = "CN_Calculate_Date__c";
        String CN_Visit_Compliance_Rate__c = "CN_Visit_Compliance_Rate__c";
        String CN_TTL_KPI_Rate__c = "CN_TTL_KPI_Rate__c";
        String CN_Bonus_Rate__c = "CN_Bonus_Rate__c";
        String CN_TTL_Volume_Rate__c = "CN_TTL_Volume_Rate__c";
        String CN_Key_SKU_Rate__c = "CN_Key_SKU_Rate__c";
        String CN_POCE_Compliance_Rate__c = "CN_POCE_Compliance_Rate__c";
        String CN_Distribution_Rate__c = "CN_Distribution_Rate__c";
        String CN_B2B_Volume_Rate__c = "CN_B2B_Volume_Rate__c";
        String CN_WTD_Visit_Compliance_Rate__c = "CN_WTD_Visit_Compliance_Rate__c";
        String CN_WTD_In_Planned_Visits__c = "CN_WTD_In_Planned_Visits__c";
        String CN_WTD_Completed_Visits__c = "CN_WTD_Completed_Visits__c";
        String CN_YearValue__c = "CN_YearValue__c";
        String CN_MonthValue__c = "CN_MonthValue__c";
        String CN_Date__c = "CN_Date__c";
        String CN_GPS_Corrects__c = "CN_GPS_Corrects__c";
        String CN_First_Day_Month__c = "CN_First_Day_Month__c";
        String CN_First_Day_Week__c = "CN_First_Day_Week__c";
        String Last_Day_Month__c = "CN_Last_Day_Month__c";
        String CN_Last_Day_Week__c = "CN_Last_Day_Week__c";
    }

    interface KPIDictFields {
        String CN_Mapping_Field__c = "CN_Mapping_Field__c";
        String CN_External_ID__c = "CN_External_ID__c";
        String CN_Desc__c = "CN_Desc__c";
        String CN_KPI__c = "CN_KPI__c";
    }
}
