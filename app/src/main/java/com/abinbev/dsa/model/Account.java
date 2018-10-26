package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountFields;
import com.abinbev.dsa.utils.AbInBevConstants.AccountRecordType;
import com.abinbev.dsa.utils.AbInBevConstants.ContactFields;
import com.abinbev.dsa.utils.AbInBevConstants.ProspectStatus;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by wandersonblough on 11/10/15.
 */
public class Account extends TranslatableSFBaseObject {

    public static final String TAG = Account.class.getName();

    public static final String ADMINISTRATOR_ROLE = "Administrator";

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("Account", AbInBevObjects.ACCOUNT)
            .putColumn("Name", AccountFields.NAME)
            .putColumn("RecordTypeId", AccountFields.RECORD_TYPE_ID)
            .putColumn("SapNumber", AccountFields.SAP_NUMBER__C)
            .putColumn("Street", AccountFields.STREET__C)
            .putColumn("Description", AccountFields.DESCRIPTION)
            .putColumn("AccountStatus", AccountFields.ACCOUNT_STATUS)
            .putColumn("ProspectStatus", AccountFields.PROSPECT_STATUS)
            .build();

    public Account() {
        super(AbInBevObjects.ACCOUNT);
    }

    public Account(JSONObject jsonObject) {
        super(AbInBevObjects.ACCOUNT, jsonObject);
    }

    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringValueForKey(AccountFields.STREET_NUMBER__C)).append(" ")
                .append(getStringValueForKey(AccountFields.STREET__C)).append("\n")
                .append(getStringValueForKey(AccountFields.NEIGHBORHOOD__C)).append("\n")
                .append(getStringValueForKey(AccountFields.MUNICIPALITY__C));
        return sb.toString();

    }

    public String getFirstAvailablePhone() {
        String phone = getMainPhoneC();
        if (!TextUtils.isEmpty(phone)) return phone;

        phone = getSecondaryPhoneC();
        if (!TextUtils.isEmpty(phone)) return phone;

        phone = getMobilePhoneC();
        if (!TextUtils.isEmpty(phone)) return phone;

        phone = getPhone();
        if (!TextUtils.isEmpty(phone)) return phone;

        return getPhoneOther();
    }

    public String getPhone() {
        //PhoneNumberUtils.formatNumber crashes on null value
        String phone = getStringValueForKey(AbInBevConstants.AccountFields.PHONE);
        if (phone == null) return "";
        else return phone;
    }

    public String getPhoneOther() {
        return getStringValueForKey(AbInBevConstants.AccountFields.PHONE_OTHER);
    }

    public String getCity() {
        return getStringValueForKey(AccountFields.CITY__C);
    }

    public String getNeighborhood() {
        return getStringValueForKey(AccountFields.NEIGHBORHOOD__C);
    }

    public double getLatitude() {
        return getDoubleValueForKey(AccountFields.LATITUDE__C);
    }

    public void setLatitude(double latitude) {
        setDoubleValueForKey(AccountFields.LATITUDE__C, latitude);
    }

    public boolean hasLatitude() {
        return !isNullOrEmpty(AccountFields.LATITUDE__C);
    }

    public double getLongitude() {
        return getDoubleValueForKey(AccountFields.LONGITUDE__C);
    }

    public void setLongitude(double longitude) {
        setDoubleValueForKey(AccountFields.LONGITUDE__C, longitude);
    }

    public void setPOCClosureRequest(boolean request) {
        setBooleanValueForKey(AccountFields.POC_CLOSURE_REQUEST, request);
    }

    public boolean getPOCClosureRequest() {
        return getBooleanValueForKey(AccountFields.POC_CLOSURE_REQUEST);
    }

    public boolean hasLongitude() {
        return !isNullOrEmpty(AccountFields.LONGITUDE__C);
    }

    public LatLng getLocation() {
        if (!hasLatitude() || !hasLongitude()) {
            return null;
        } else {
            double lat = getLatitude();
            double lon = getLongitude();
            return new LatLng(lat, lon);
        }
    }

    public void setLocation(LatLng location) {
        if (location == null) {
            setNullValue(AccountFields.LATITUDE__C);
            setNullValue(AccountFields.LONGITUDE__C);
        } else {
            setLatitude(location.latitude);
            setLongitude(location.longitude);
        }
    }

    public double getAvailableCredit() {
        return getDoubleValueForKey(AccountFields.CREDITO_DISPONIBLE);
    }

    public double getCreditLimit() {
        return getDoubleValueForKey(AccountFields.LIMITE_CREDITO);
    }

    public String getNextVisit() {
        return getStringValueForKey(AbInBevConstants.AccountFields.PROXIMA_VISITA_BD_C);
    }

    public String getTypeOfCare() {
        return getStringValueForKey(AccountFields.INDICADOR_TV_O_PV__C);
    }

    public String getNextCall() {
        return getStringValueForKey(AccountFields.PROXIMA_LLAMADA_TV__C);
    }

    public String getSaleVolume() {
        return getStringValueForKey(AccountFields.CI_VOL_VENTAS__C);
    }

    public String getSaleVolumeName() {
        JSONObject jsonObject = getJsonObject(AccountFields.CI_VOL_VENTAS__R);
        return jsonObject == null ? null : jsonObject.optString(StdFields.NAME, null);
    }

    public String getSubchannelLo() {
        return getStringValueForKey(AccountFields.CAN_SUBCANAL_LO__C);
    }

    public String getSubchannelLoName() {
        JSONObject jsonObject = getJsonObject(AccountFields.CAN_SUBCANAL_LO__R);
        return jsonObject == null ? null : jsonObject.optString(StdFields.NAME, null);
    }

    public String getSubchannelReg() {
        return getStringValueForKey(AccountFields.CAN_SUBCANAL_REG__C);
    }

    public String getSubchannelRegName() {
        JSONObject jsonObject = getJsonObject(AccountFields.CAN_SUBCANAL_REG__R);
        return jsonObject == null ? null : jsonObject.optString(StdFields.NAME, null);
    }

    public String getCiaMedaSecun() {
        return getStringValueForKey(AccountFields.CIA_MEDA_SECUN__C);
    }
    public String isB2BRegistered() {
        return getStringValueForKey(AccountFields.Is_B2B_Registered__c);
    }

    public String getB2BCode() {
        return getStringValueForKey(AccountFields.B2B_Code__c);
    }
    public String getCiaMedaSecunName() {
        String ciaMedaSecunId = getStringValueForKey(AccountFields.CIA_MEDA_SECUN__C);
        Parametro__c parameter = Parametro__c.getById(ciaMedaSecunId);
        if (parameter != null) {
            return parameter.getName();
        }

        return null;
    }

    public String getCiaMedaSecunType() {
        JSONObject jsonObject = getJsonObject(AccountFields.CIA_MEDA_SECUN__R);
        return jsonObject == null ? null : jsonObject.optString(AccountFields.TIPO_PARAMETRO__C, null);
    }

    public String getCodigoDelCliente__c() {
        return getStringValueForKey(AccountFields.CODIGO_DEL_CLIENTE__C);
    }

    public void setSAPNumber(String SAPnumber) {
        setStringValueForKey(AccountFields.SAP_NUMBER__C, SAPnumber);
    }

    public String getSAPNumber() {
        return getStringValueForKey(AccountFields.SAP_NUMBER__C);
    }

    public String getSegmento_negociacion__c() {
        return getStringValueForKey(AccountFields.SEGMENTO_NEGOCIACION__C);
    }

    public String getCentroDistribucion__c() {
        return getStringValueForKey(AccountFields.CENTRO_DISTRIBUCION__C);
    }

    public String getNegotiationStatus() {
        return getStringValueForKey(AccountFields.NEGOTIATION_STATUS__C);
    }

    public String getTranslatedNegotiationStatus() {
        return getTranslatedStringValueForKey(AccountFields.NEGOTIATION_STATUS__C);
    }

    public String getLeadSource() {
        return getStringValueForKey(AccountFields.CN_LEAD_SOURCE__C);
    }

    public String getTranslatedLeadSource() {
        return getTranslatedStringValueForKey(AccountFields.CN_LEAD_SOURCE__C);
    }

    public void setLeadSource(String leadSource) {
        setStringValueForKey(AccountFields.CN_LEAD_SOURCE__C, leadSource);
    }

    public boolean hasAccountStatus() {
        return !isNullOrEmpty(AccountFields.ACCOUNT_STATUS);
    }

    public String getAccountStatus() {
        return getStringValueForKey(AccountFields.ACCOUNT_STATUS);
    }

    public String getName() {
        return getStringValueForKey(StdFields.NAME);
    }

    public void setName(String name) {
        setStringValueForKey(StdFields.NAME, name);
    }

    public String getStreet() {
        return getStringValueForKey(AccountFields.STREET__C);
    }

    public String getVisitPeriod() {
        return getStringValueForKey(AccountFields.VISIT_PERIOD);
    }

    public void setStreet(String street) {
        setStringValueForKey(AccountFields.STREET__C, street);
    }

    public String getStreetNumber() {
        return getStringValueForKey(AccountFields.STREET_NUMBER__C);
    }

    public void setStreetNumber(String street) {
        setStringValueForKey(AccountFields.STREET_NUMBER__C, street);
    }

    public void setNeighborhood(String neighborhood) {
        setStringValueForKey(AccountFields.NEIGHBORHOOD__C, neighborhood);
    }

    public String getColony() {
        return getStringValueForKey(AccountFields.COLONY__C);
    }

    public void setColony(String colony) {
        setStringValueForKey(AccountFields.COLONY__C, colony);
    }

    public String getMunicipality() {
        return getStringValueForKey(AccountFields.MUNICIPALITY__C);
    }

    public void setMunicipality(String municipality) {
        setStringValueForKey(AccountFields.MUNICIPALITY__C, municipality);
    }

    public String getProvince() {
        return getStringValueForKey(AccountFields.PROVINCE_C);
    }

    public void setProvince(String province) {
        setStringValueForKey(AccountFields.PROVINCE_C, province);
    }

    public String getPreference() {
        return getStringValueForKey(AccountFields.PREFERENCE__C);
    }

    public void setPreference(String preference) {
        setStringValueForKey(AccountFields.PREFERENCE__C, preference);
    }

    public String getClassification() {
        return getStringValueForKey(AccountFields.CLASSIFICATION__C);
    }

    public void setClassification(String classification) {
        setStringValueForKey(AccountFields.CLASSIFICATION__C, classification);
    }

    public String getBusinessUnit() {
        return getStringValueForKey(AccountFields.BUSINESS_UNIT__C);
    }

    public void setBusinessUnit(String businessUnit) {
        setStringValueForKey(AccountFields.BUSINESS_UNIT__C, businessUnit);
    }

    public String getSalesOffice() {
        return getStringValueForKey(AccountFields.SALES_OFFICE__C);
    }

    public void setLocalOwnerC(String localOwnerC) {
        setStringValueForKey(AccountFields.LOCAL_OWNER__C, localOwnerC);
    }

    public String getLocalOwnerC() {
        return getStringValueForKey(AccountFields.LOCAL_OWNER__C);
    }

    public void setSalesOffice(String salesOffice) {
        setStringValueForKey(AccountFields.SALES_OFFICE__C, salesOffice);
    }

    public String getContactFirstNameC() {
        return getStringValueForKey(AccountFields.CONTACT_FIRST_NAME__C);
    }

    public void setContactFirstNameC(String contactFirstNameC) {
        setStringValueForKey(AccountFields.CONTACT_FIRST_NAME__C, contactFirstNameC);
    }

    public String getContactLastNameC() {
        return getStringValueForKey(AccountFields.CONTACT_LAST_NAME__C);
    }

    public void setContactLastNameC(String contactLastNameC) {
        setStringValueForKey(AccountFields.CONTACT_LAST_NAME__C, contactLastNameC);
    }

    public String getMainPhoneC() {
        return getStringValueForKey(AccountFields.MAIN_PHONE__C);
    }

    public void setMainPhoneC(String mainPhoneC) {
        setStringValueForKey(AccountFields.MAIN_PHONE__C, mainPhoneC);
    }

    public String getSecondaryPhoneC() {
        return getStringValueForKey(AccountFields.SECONDARY_PHONE__C);
    }

    public void setSecondaryPhoneC(String secondaryPhoneC) {
        setStringValueForKey(AccountFields.SECONDARY_PHONE__C, secondaryPhoneC);
    }

    public String getMobilePhoneC() {
        return getStringValueForKey(AccountFields.MOBILE_PHONE__C);
    }

    public void setMobilePhoneC(String mobilePhoneC) {
        setStringValueForKey(AccountFields.MOBILE_PHONE__C, mobilePhoneC);
    }

    public String getEmailC() {
        return getStringValueForKey(AccountFields.EMAIL__C);
    }

    public void setEmailC(String emailC) {
        setStringValueForKey(AccountFields.EMAIL__C, emailC);
    }

    public String getPreferredServiceDayC() {
        return getStringValueForKey(AccountFields.PREFERRED_SERVICE_DAYS__C);
    }

    public void setPreferredServiceDayC(String preferredServiceDayC) {
        setStringValueForKey(AccountFields.PREFERRED_SERVICE_DAYS__C, preferredServiceDayC);
    }

    public String getPreferredServiceHourC() {
        return getStringValueForKey(AccountFields.PREFERRED_SERVICE_HOURS__C);
    }

    public void setPreferredServiceHourC(String preferredServiceHourC) {
        setStringValueForKey(AccountFields.PREFERRED_SERVICE_HOURS__C, preferredServiceHourC);
    }

    public String getSegmentC() {
        return getStringValueForKey(AccountFields.SEGMENT__C);
    }

    public void setSegmentC(String segmentC) {
        setStringValueForKey(AccountFields.SEGMENT__C, segmentC);
    }

    public String getVolumeCurrentC() {
        return getStringValueForKey(AccountFields.VOLUME_CURRENT__C);
    }

    public void setVolumeCurrentC(String volumeCurrentC) {
        setStringValueForKey(AccountFields.VOLUME_CURRENT__C, volumeCurrentC);
    }

    public String getPreferredSizeC() {
        return getStringValueForKey(AccountFields.PREFERRED_SIZE__C);
    }

    public void setPreferredSizeC(String importante) {
        setStringValueForKey(AccountFields.PREFERRED_SIZE__C, importante);
    }

    public String getEconomicActivityC() {
        return getStringValueForKey(AccountFields.ECONOMIC_ACTIVITY__C);
    }

    public void setEconomicActivityC(String economicActivityC) {
        setStringValueForKey(AccountFields.ECONOMIC_ACTIVITY__C, economicActivityC);
    }

    public String getLicenseC() {
        return getStringValueForKey(AccountFields.LICENSE__C);
    }

    public void setLicenseC(String licenseC) {
        setStringValueForKey(AccountFields.LICENSE__C, licenseC);
    }

    public String getBusinessFormatC() {
        return getStringValueForKey(AccountFields.BUSINESS_FORMAT__C);
    }

    public void setBusinessFormatC(String businessFormatC) {
        setStringValueForKey(AccountFields.BUSINESS_FORMAT__C, businessFormatC);
    }

    public String getPostalCodeC() {
        return getStringValueForKey(AccountFields.POSTAL_CODE__C);
    }

    public void setPostalCodeC(String postalCodeC) {
        setStringValueForKey(AccountFields.POSTAL_CODE__C, postalCodeC);
    }

    public String getLoan() {
        return getStringValueForKey(AccountFields.LOAN__C);
    }

    public void setLoan(String postalCodeC) {
        setStringValueForKey(AccountFields.LOAN__C, postalCodeC);
    }

    public String getLoanAmountC() {
        return getStringValueForKey(AccountFields.LOAN_AMOUNT__C);
    }

    public void setLoanAmountC(String loanAmountC) {
        setStringValueForKey(AccountFields.LOAN_AMOUNT__C, loanAmountC);
    }

    public String getLoanEndDateC() {
        return getStringValueForKey(AccountFields.LOAN_END_DATE__C);
    }

    public void setLoanEndDateC(String loanEndDateC) {
        setStringValueForKey(AccountFields.LOAN_END_DATE__C, loanEndDateC);
    }

    public String getCreditAmountC() {
        return getStringValueForKey(AccountFields.CREDIT_AMOUNT__C);
    }

    public void setCreditAmountC(String creditAmountC) {
        setStringValueForKey(AccountFields.CREDIT_AMOUNT__C, creditAmountC);
    }

    public String getCreditTermLengthC() {
        return getStringValueForKey(AccountFields.CREDIT_TERM_LENGTH__C);
    }

    public void setCreditTermLengthC(String creditTermLengthC) {
        setStringValueForKey(AccountFields.CREDIT_TERM_LENGTH__C, creditTermLengthC);
    }

    public String getNumberOfRefrigeratorDoorsC() {
        return getStringValueForKey(AccountFields.NUMBER_OF_REFRIGERATOR_DOORS__C);
    }

    public void setNumberOfRefrigeratorDoorsC(String numberOfRefrigeratorDoorsC) {
        setStringValueForKey(AccountFields.NUMBER_OF_REFRIGERATOR_DOORS__C, numberOfRefrigeratorDoorsC);
    }

    public String getRefrigeratorC() {
        return getStringValueForKey(AccountFields.REFRIGERATOR__C);
    }

    public void setRefrigeratorC(String refrigeratorC) {
        setStringValueForKey(AccountFields.REFRIGERATOR__C, refrigeratorC);
    }

    public String getFacadeBrandingC() {
        return getStringValueForKey(AccountFields.FACADE_BRANDING__C);
    }

    public void setFacadeBrandingC(String facadeBrandingC) {
        setStringValueForKey(AccountFields.FACADE_BRANDING__C, facadeBrandingC);
    }

    public String getNumberOfMaterialC() {
        return getStringValueForKey(AccountFields.NUMBER_OF_MATERIAL__C);
    }

    public void setNumberOfMaterialC(String numberOfMaterialC) {
        setStringValueForKey(AccountFields.NUMBER_OF_MATERIAL__C, numberOfMaterialC);
    }

    public boolean getNotInterestedC() {
        return getBooleanValueForKey(AccountFields.NOT_INTERESTED__C);
    }

    public void setNotInterestedC(boolean notInterestedC) {
        setBooleanValueForKey(AccountFields.NOT_INTERESTED__C, notInterestedC);
    }

    public String getCoolerC() {
        return getStringValueForKey(AccountFields.COOLER__C);
    }

    public void setCoolerC(String coolerC) {
        setStringValueForKey(AccountFields.COOLER__C, coolerC);
    }

    public String getCategory() {
        return getStringValueForKey(AccountFields.CATEGORY);
    }

    public void setCategory(String category) {
        setStringValueForKey(AccountFields.CATEGORY, category);
    }

    public String getChannel() {
        return getStringValueForKey(AccountFields.CHANNEL);
    }

    public void setChannel(String channel) {
        setStringValueForKey(AccountFields.CHANNEL, channel);
    }

    public String getCityRegion() {
        return getStringValueForKey(AccountFields.CITY_REGION);
    }

    public void setCityRegion(String cityRegion) {
        setStringValueForKey(AccountFields.CITY_REGION, cityRegion);
    }

    public String getProspectStatus() {
        return getStringValueForKey(AccountFields.PROSPECT_STATUS);
    }

    public String getTranslatedProspectStatus() {
        return getTranslatedStringValueForKey(AccountFields.PROSPECT_STATUS);
    }

    public void setProspectStatus(String status) {
        setStringValueForKey(AccountFields.PROSPECT_STATUS, status);
    }

    public String getType() {
        return getStringValueForKey(AccountFields.TYPE);
    }

    public void changeProspectStatusCheckedIn() {
        if (!isProspect()) return;

        String currentStatus = getProspectStatus();
        if (isNullOrEmpty(AccountFields.PROSPECT_STATUS)
                || ProspectStatus.OPEN.equals(currentStatus)) {

            setProspectStatus(ProspectStatus.CONTACTED);
            changeProspectStatusDataUpdated();
        }
    }

    public void changeProspectStatusDataUpdated() {
        if (!isProspect()) return;

        String currentStatus = getProspectStatus();
        if (ProspectStatus.CONTACTED.equals(currentStatus)
                && hasBasicData()
                && hasAdditionalData()
                && ProspectStatus.CONTACTED.equals(getProspectStatus())) {

            setProspectStatus(ProspectStatus.CONTACTED);
//            changeProspectStatusNegotiationUpdated();
        }
    }

//    public void changeProspectStatusNegotiationUpdated() {
//        if (!isProspect()) return;
//
//        String currentStatus = getProspectStatus();
//        if (ProspectStatus.DATA_VALIDATED.equals(currentStatus)) {
//            List<CN_Product_Negotiation__c> negotiations = CN_Product_Negotiation__c.getNegotiationsByAccountId(getId());
//            if (!negotiations.isEmpty()) {
//                if (hasCompletedNegotiation(negotiations)) {
//                    setProspectStatus(ProspectStatus.NEGOTIATION_COMPLETED);
//                    changeProspectStatusConversionSent();
//                } else {
//                    setProspectStatus(ProspectStatus.IN_NEGOTIATION);
//                }
//            }
//        } else if (ProspectStatus.IN_NEGOTIATION.equals(currentStatus)) {
//            List<CN_Product_Negotiation__c> negotiations = CN_Product_Negotiation__c.getNegotiationsByAccountId(getId());
//            if (hasCompletedNegotiation(negotiations)) {
//                setProspectStatus(ProspectStatus.NEGOTIATION_COMPLETED);
//                changeProspectStatusConversionSent();
//            }
//        }
//    }

    private boolean hasCompletedNegotiation(List<CN_Product_Negotiation__c> negotiations) {
        for (CN_Product_Negotiation__c negotiation : negotiations) {
            if (negotiation.isCompleted()) return true;
        }
        return false;
    }

    public void changeProspectStatusConversionSent() {
        if (!isProspect()) return;

        String currentStatus = getProspectStatus();
        if (ProspectStatus.CONTACTED.equals(currentStatus)) {
                setProspectStatus(ProspectStatus.SUBMITTED);
        }
    }

    public void changeProspectStatusUnqualified() {
        String currentStatus = getProspectStatus();
        if (ProspectStatus.CONTACTED.equals(currentStatus)) {
            setProspectStatus(ProspectStatus.UNQUALIFIED);
        }
    }

    public boolean hasOwnerAssignedDate() {
        return !isNullOrEmpty(AccountFields.OWNER_ASSIGNED_DATE);
    }

    public void setOwnerAssignedDate(String date) {
        setStringValueForKey(AccountFields.OWNER_ASSIGNED_DATE, date);
    }

    public boolean hasPocType() {
        return !isNullOrEmpty(AccountFields.POC_TYPE);
    }

    public void setPocType(String type) {
        setStringValueForKey(AccountFields.POC_TYPE, type);
    }

    public static Account getById(String accountId) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, accountId, Account.class);
    }

    public static Contact getPrimaryContactForAccountId(String accountId) {

        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'", DSAObjects.CONTACT,
                ContactFields.ACCOUNT_ID, accountId, DSAObjects.CONTACT, ContactFields.ROLE, Account.ADMINISTRATOR_ROLE);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CONTACT, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        JSONArray jsonArray = recordsArray.optJSONArray(0);
        JSONObject jsonObject = jsonArray == null ? null : jsonArray.optJSONObject(0);

        return jsonObject == null ? null : new Contact(jsonObject);
    }

    public static String getLastVisitDateForAccountId(String accountId) {

        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s' ORDER BY {%s:%s} DESC LIMIT 1",
                DSAObjects.EVENT, AbInBevConstants.EventFields.WHAT_ID, accountId,
                DSAObjects.EVENT, AbInBevConstants.EventFields.ESTADO_DE_VISITA__C, AbInBevConstants.EventFields.STATUS_COMPLETE,
                DSAObjects.EVENT, DSAConstants.EventFields.END_DATE_TIME);

        // We assume that StartDateTime is equal to ActivityDateTime.
        String smartSql = String.format(DSAConstants.Formats.SQL_FORMAT, DSAObjects.EVENT,
                DSAConstants.EventFields.START_DATE_TIME, smartSqlFilter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        JSONArray jsonArray = recordsArray.optJSONArray(0);
        String jsonObject = jsonArray == null ? null : jsonArray.optString(0);

        if (jsonObject != null) {
            try {
                // Convert server utc time to local time zone.
                Date date = DateUtils.SERVER_DATE_TIME_FORMAT.parse(String.valueOf(jsonObject));
                return DateUtils.dateToDateString(date);
            } catch (ParseException e) {
                Log.w(TAG, "Error while parsing date.", e);
            }
        }
        return null;
    }

    public String getReferencedValue(String objectReferenced, String fieldName) {
        try {
            String Id = getStringValueForKey(fieldName);
            if (Id == null) return null;
            JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(objectReferenced, "Id", Id);
            if (jsonObject != null) {
                return jsonObject.optString("Name", null);
            }
        } catch (Exception e) {
            Log.e("AccountReference", "Investigate the possible cause: " + objectReferenced + " : " + fieldName + "\n" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public String getStringValueForKey(String key) {
        return super.getStringValueForKey(key);
    }

    public static String getSegmentForAccount(String accountId) {
        Account account = getById(accountId);
        return account.getSegmento_negociacion__c();
    }

    public String getLastVisit() {
        return getStringValueForKey(AccountFields.ULTIMA_VISITA__C);
    }

    public void setLastVisit(String date) {
        setStringValueForKey(AccountFields.ULTIMA_VISITA__C, date);
    }

    public void setLastVisit(Date date) {
        setStringValueForKey(AccountFields.ULTIMA_VISITA__C, DateUtils.dateToDateString(date));
    }

    public void setRecordTypeId(String recordTypeId) {
        setStringValueForKey(AccountFields.RECORD_TYPE_ID, recordTypeId);
    }

    public String getRecordTypeId() {
        return getStringValueForKey(AccountFields.RECORD_TYPE_ID);
    }
    public String getD1_Technician(){
        return getStringValueForKey(AccountFields.D1_TECHNICIAN__C);
    }

    public boolean hasBasicData() {
        return ContentUtils.isStringValid(getName()) &&
                ContentUtils.isStringValid(getStreet()) &&
                ContentUtils.isStringValid(getContactFirstNameC()) &&
                ContentUtils.isStringValid(getContactLastNameC()) &&
                ContentUtils.isStringValid(getMainPhoneC()) &&
                ContentUtils.isStringValid(getProspectStatus()) &&
                Attachment.getAccountPhotoAttachment(getId()) != null ;
//                && Attachment.getAccountLicensePhotoAttachment(getId()) != null;
    }

    // we can refine this later
    public boolean hasAdditionalData() {
        return hasBasicData();
    }

    public boolean isProspect() {
        String recordTypeId = this.getRecordTypeId();
        RecordType recordType = RecordType.getById(recordTypeId);
        return recordType != null && AccountRecordType.PROSPECT.equals(recordType.getName());
    }

    public static List<Account> getAccounts() {
        String smartSqlFilter = String.format(" ORDER BY {%s:%s} ASC",
                AbInBevObjects.ACCOUNT, StdFields.NAME);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_ALL_FORMAT, AbInBevObjects.ACCOUNT).concat(smartSqlFilter);
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, Account.class);
    }

    private static String getActiveAccountsFilter(String searchText, List<String> recordTypeIds) {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);

        String smartSqlFilter = "({Account:AccountStatus} IS NULL " +
                "OR {Account:AccountStatus} NOT IN ('Blocked', 'Inactive Client'))";

        if (recordTypeIds != null && !recordTypeIds.isEmpty()) {
            String recordTypeIdsString = TextUtils.join("', '", recordTypeIds);
            fv.putValue("recordTypeId", recordTypeIdsString);
            smartSqlFilter += " AND {Account:RecordTypeId} IN ('{recordTypeId}')";
        }

        if (!TextUtils.isEmpty(searchText)) {
            fv.putValue("searchText", searchText);

            smartSqlFilter +=
                    " AND (" +
                            "{Account:Name} LIKE '%{searchText}%' " +
                            "OR {Account:SapNumber} LIKE '%{searchText}%' " +
                            "OR {Account:Street} LIKE '%{searchText}%' " +
                            "OR {Account:Description} LIKE '%{searchText}%'" +
                            ")";
        }
        return DataManagerUtils.format(smartSqlFilter, fv);
    }

    private static String getActiveAccountsFilter(String searchText, List<String> recordTypeIds, String prospectStatus) {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);

        String smartSqlFilter = getActiveAccountsFilter(searchText, recordTypeIds);

        if (!TextUtils.isEmpty(prospectStatus)) {
            fv.putValue("prospectStatus", prospectStatus);
            smartSqlFilter += " AND {Account:ProspectStatus} IN ('{prospectStatus}')";
        }

        return DataManagerUtils.format(smartSqlFilter, fv);
    }


    private static String getActiveAccountsFilter(String searchText, List<String> recordTypeIds, String prospectStatus, Date prospectCreationDate) {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);

        String smartSqlFilter = getActiveAccountsFilter(searchText, recordTypeIds, prospectStatus);

        if (prospectCreationDate != null) {
            fv.putValue("prospectCreationDate", DateUtils.dateToDateString(prospectCreationDate));
            smartSqlFilter += " AND ({Account:CreatedDate} LIKE '{prospectCreationDate}%')";
        }

        return DataManagerUtils.format(smartSqlFilter, fv);
    }

    private static String getActiveAccountsFilter(String searchText, List<String> recordTypeIds, String prospectStatus, Date startDate, Date endDate) {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);

        String smartSqlFilter = getActiveAccountsFilter(searchText, recordTypeIds, prospectStatus);

        if (null != startDate) {
            fv.putValue("startDate", DateUtils.SERVER_DATE_TIME_FORMAT.format(startDate));
            smartSqlFilter += " AND {Account:CreatedDate} >= '{startDate}'";
        }

        if (null != endDate) {
            fv.putValue("endDate", DateUtils.SERVER_DATE_TIME_FORMAT.format(endDate));
            smartSqlFilter += " AND {Account:CreatedDate} <= '{endDate}'";
        }

        return DataManagerUtils.format(smartSqlFilter, fv);
    }

    public static int getActiveAccountsCount(String searchText, List<String> recordTypeIds, String prospectStatus, Date prospectCreationDate) {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("accountFilter", getActiveAccountsFilter(searchText, recordTypeIds, prospectStatus, prospectCreationDate));
        String smartSql = "SELECT count() FROM {Account} WHERE {accountFilter}";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, smartSql, fv);
    }

    public static int getActiveAccountsCount(String searchText, List<String> recordTypeIds) {
        return getActiveAccountsCount(searchText, recordTypeIds, null, null);
    }

    public static List<Account> getActiveAccountsForSearchText(String searchText, List<String> recordTypeIds, String prospectStatus, Date prospectCreationDate, int pageIndex, int pageSize) {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("accountFilter", getActiveAccountsFilter(searchText, recordTypeIds, prospectStatus, prospectCreationDate));
        fv.putValue("pageOffset", pageIndex * pageSize);
        fv.putValue("pageSize", pageSize);

        String smartSql = "SELECT {Account:_soup} FROM {Account} " +
                "WHERE {accountFilter} " +
                "ORDER BY {Account:CreatedDate} DESC " +
                "LIMIT {pageSize} OFFSET {pageOffset}";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, fv, Account.class);
    }


    public static List<Account> getActiveAccountsForSearchText(String searchText, List<String> recordTypeIds, int pageIndex, int pageSize) {
        return getActiveAccountsForSearchText(searchText, recordTypeIds, null, null, pageIndex, pageSize);
    }

    public static List<Account> getActiveAccountsForSearchText(String searchText) {
        return getActiveAccountsForSearchText(searchText, null, 0, 50);
    }

    public static int getActiveProspectsCount(String prospectStatus, Date startDate, Date endDate) {
        if (TextUtils.isEmpty(prospectStatus)) {
            return 0;
        }
        String recordName = AccountRecordType.PROSPECT;
        List<String> recordTypeIds = RecordType.getRecordIdsListByNameAndObjectType(recordName, AbInBevConstants.AbInBevObjects.ACCOUNT);
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("accountFilter", getActiveAccountsFilter(null, recordTypeIds, prospectStatus, startDate, endDate));
        String smartSql = "SELECT count() FROM {Account} WHERE {accountFilter}";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, smartSql, fv);
    }

    public static Account getAccountForId(String id) {
        String sql = "SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} = '%3$s'";
        String smartSql = String.format(sql, AbInBevObjects.ACCOUNT, AbInBevConstants.ID, id);
        Account account = DataManagerUtils.fetchObject(DataManagerFactory.getDataManager(), smartSql, Account.class);
        return account;
    }

}
