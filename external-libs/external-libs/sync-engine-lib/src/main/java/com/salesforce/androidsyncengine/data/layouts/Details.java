package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
public class Details {

    @SerializedName("autoNumber")
    @Expose
    private Boolean autoNumber;
    @SerializedName("byteLength")
    @Expose
    private Integer byteLength;
    @SerializedName("calculated")
    @Expose
    private Boolean calculated;
    @SerializedName("calculatedFormula")
    @Expose
    private String calculatedFormula;
    @SerializedName("cascadeDelete")
    @Expose
    private Boolean cascadeDelete;
    @SerializedName("caseSensitive")
    @Expose
    private Boolean caseSensitive;
    @SerializedName("controllerName")
    @Expose
    private Object controllerName;
    @SerializedName("createable")
    @Expose
    private Boolean createable;
    @SerializedName("custom")
    @Expose
    private Boolean custom;
    @SerializedName("defaultValue")
    @Expose
    private Object defaultValue;
    @SerializedName("defaultValueFormula")
    @Expose
    private Object defaultValueFormula;
    @SerializedName("defaultedOnCreate")
    @Expose
    private Boolean defaultedOnCreate;
    @SerializedName("dependentPicklist")
    @Expose
    private Boolean dependentPicklist;
    @SerializedName("deprecatedAndHidden")
    @Expose
    private Boolean deprecatedAndHidden;
    @SerializedName("digits")
    @Expose
    private Integer digits;
    @SerializedName("displayLocationInDecimal")
    @Expose
    private Boolean displayLocationInDecimal;
    @SerializedName("encrypted")
    @Expose
    private Boolean encrypted;
    @SerializedName("externalId")
    @Expose
    private Boolean externalId;
    @SerializedName("extraTypeInfo")
    @Expose
    private Object extraTypeInfo;
    @SerializedName("filterable")
    @Expose
    private Boolean filterable;
    @SerializedName("filteredLookupInfo")
    @Expose
    private Object filteredLookupInfo;
    @SerializedName("groupable")
    @Expose
    private Boolean groupable;
    @SerializedName("highScaleNumber")
    @Expose
    private Boolean highScaleNumber;
    @SerializedName("htmlFormatted")
    @Expose
    private Boolean htmlFormatted;
    @SerializedName("idLookup")
    @Expose
    private Boolean idLookup;
    @SerializedName("inlineHelpText")
    @Expose
    private Object inlineHelpText;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("length")
    @Expose
    private Integer length;
    @SerializedName("mask")
    @Expose
    private Object mask;
    @SerializedName("maskType")
    @Expose
    private Object maskType;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("nameField")
    @Expose
    private Boolean nameField;
    @SerializedName("namePointing")
    @Expose
    private Boolean namePointing;
    @SerializedName("nillable")
    @Expose
    private Boolean nillable;
    @SerializedName("permissionable")
    @Expose
    private Boolean permissionable;
    @SerializedName("picklistValues")
    @Expose
    private List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
    @SerializedName("precision")
    @Expose
    private Integer precision;
    @SerializedName("queryByDistance")
    @Expose
    private Boolean queryByDistance;
    @SerializedName("referenceTargetField")
    @Expose
    private Object referenceTargetField;
    @SerializedName("referenceTo")
    @Expose
    private List<String> referenceTo = new ArrayList<>();
    @SerializedName("relationshipName")
    @Expose
    private Object relationshipName;
    @SerializedName("relationshipOrder")
    @Expose
    private Object relationshipOrder;
    @SerializedName("restrictedDelete")
    @Expose
    private Boolean restrictedDelete;
    @SerializedName("restrictedPicklist")
    @Expose
    private Boolean restrictedPicklist;
    @SerializedName("scale")
    @Expose
    private Integer scale;
    @SerializedName("soapType")
    @Expose
    private String soapType;
    @SerializedName("sortable")
    @Expose
    private Boolean sortable;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("unique")
    @Expose
    private Boolean unique;
    @SerializedName("updateable")
    @Expose
    private Boolean updateable;
    @SerializedName("writeRequiresMasterRead")
    @Expose
    private Boolean writeRequiresMasterRead;

    /**
     *
     * @return
     * The autoNumber
     */
    public Boolean getAutoNumber() {
        return autoNumber;
    }

    /**
     *
     * @param autoNumber
     * The autoNumber
     */
    public void setAutoNumber(Boolean autoNumber) {
        this.autoNumber = autoNumber;
    }

    /**
     *
     * @return
     * The byteLength
     */
    public Integer getByteLength() {
        return byteLength;
    }

    /**
     *
     * @param byteLength
     * The byteLength
     */
    public void setByteLength(Integer byteLength) {
        this.byteLength = byteLength;
    }

    /**
     *
     * @return
     * The calculated
     */
    public Boolean getCalculated() {
        return calculated;
    }

    /**
     *
     * @param calculated
     * The calculated
     */
    public void setCalculated(Boolean calculated) {
        this.calculated = calculated;
    }

    /**
     *
     * @return
     * The calculatedFormula
     */
    public String getCalculatedFormula() {
        return calculatedFormula;
    }

    /**
     *
     * @param calculatedFormula
     * The calculatedFormula
     */
    public void setCalculatedFormula(String calculatedFormula) {
        this.calculatedFormula = calculatedFormula;
    }

    /**
     *
     * @return
     * The cascadeDelete
     */
    public Boolean getCascadeDelete() {
        return cascadeDelete;
    }

    /**
     *
     * @param cascadeDelete
     * The cascadeDelete
     */
    public void setCascadeDelete(Boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }

    /**
     *
     * @return
     * The caseSensitive
     */
    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    /**
     *
     * @param caseSensitive
     * The caseSensitive
     */
    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     *
     * @return
     * The controllerName
     */
    public Object getControllerName() {
        return controllerName;
    }

    /**
     *
     * @param controllerName
     * The controllerName
     */
    public void setControllerName(Object controllerName) {
        this.controllerName = controllerName;
    }

    /**
     *
     * @return
     * The createable
     */
    public Boolean getCreateable() {
        return createable;
    }

    /**
     *
     * @param createable
     * The createable
     */
    public void setCreateable(Boolean createable) {
        this.createable = createable;
    }

    /**
     *
     * @return
     * The custom
     */
    public Boolean getCustom() {
        return custom;
    }

    /**
     *
     * @param custom
     * The custom
     */
    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    /**
     *
     * @return
     * The defaultValue
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     *
     * @param defaultValue
     * The defaultValue
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     *
     * @return
     * The defaultValueFormula
     */
    public Object getDefaultValueFormula() {
        return defaultValueFormula;
    }

    /**
     *
     * @param defaultValueFormula
     * The defaultValueFormula
     */
    public void setDefaultValueFormula(Object defaultValueFormula) {
        this.defaultValueFormula = defaultValueFormula;
    }

    /**
     *
     * @return
     * The defaultedOnCreate
     */
    public Boolean getDefaultedOnCreate() {
        return defaultedOnCreate;
    }

    /**
     *
     * @param defaultedOnCreate
     * The defaultedOnCreate
     */
    public void setDefaultedOnCreate(Boolean defaultedOnCreate) {
        this.defaultedOnCreate = defaultedOnCreate;
    }

    /**
     *
     * @return
     * The dependentPicklist
     */
    public Boolean getDependentPicklist() {
        return dependentPicklist;
    }

    /**
     *
     * @param dependentPicklist
     * The dependentPicklist
     */
    public void setDependentPicklist(Boolean dependentPicklist) {
        this.dependentPicklist = dependentPicklist;
    }

    /**
     *
     * @return
     * The deprecatedAndHidden
     */
    public Boolean getDeprecatedAndHidden() {
        return deprecatedAndHidden;
    }

    /**
     *
     * @param deprecatedAndHidden
     * The deprecatedAndHidden
     */
    public void setDeprecatedAndHidden(Boolean deprecatedAndHidden) {
        this.deprecatedAndHidden = deprecatedAndHidden;
    }

    /**
     *
     * @return
     * The digits
     */
    public Integer getDigits() {
        return digits;
    }

    /**
     *
     * @param digits
     * The digits
     */
    public void setDigits(Integer digits) {
        this.digits = digits;
    }

    /**
     *
     * @return
     * The displayLocationInDecimal
     */
    public Boolean getDisplayLocationInDecimal() {
        return displayLocationInDecimal;
    }

    /**
     *
     * @param displayLocationInDecimal
     * The displayLocationInDecimal
     */
    public void setDisplayLocationInDecimal(Boolean displayLocationInDecimal) {
        this.displayLocationInDecimal = displayLocationInDecimal;
    }

    /**
     *
     * @return
     * The encrypted
     */
    public Boolean getEncrypted() {
        return encrypted;
    }

    /**
     *
     * @param encrypted
     * The encrypted
     */
    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     *
     * @return
     * The externalId
     */
    public Boolean getExternalId() {
        return externalId;
    }

    /**
     *
     * @param externalId
     * The externalId
     */
    public void setExternalId(Boolean externalId) {
        this.externalId = externalId;
    }

    /**
     *
     * @return
     * The extraTypeInfo
     */
    public Object getExtraTypeInfo() {
        return extraTypeInfo;
    }

    /**
     *
     * @param extraTypeInfo
     * The extraTypeInfo
     */
    public void setExtraTypeInfo(Object extraTypeInfo) {
        this.extraTypeInfo = extraTypeInfo;
    }

    /**
     *
     * @return
     * The filterable
     */
    public Boolean getFilterable() {
        return filterable;
    }

    /**
     *
     * @param filterable
     * The filterable
     */
    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    /**
     *
     * @return
     * The filteredLookupInfo
     */
    public Object getFilteredLookupInfo() {
        return filteredLookupInfo;
    }

    /**
     *
     * @param filteredLookupInfo
     * The filteredLookupInfo
     */
    public void setFilteredLookupInfo(Object filteredLookupInfo) {
        this.filteredLookupInfo = filteredLookupInfo;
    }

    /**
     *
     * @return
     * The groupable
     */
    public Boolean getGroupable() {
        return groupable;
    }

    /**
     *
     * @param groupable
     * The groupable
     */
    public void setGroupable(Boolean groupable) {
        this.groupable = groupable;
    }

    /**
     *
     * @return
     * The highScaleNumber
     */
    public Boolean getHighScaleNumber() {
        return highScaleNumber;
    }

    /**
     *
     * @param highScaleNumber
     * The highScaleNumber
     */
    public void setHighScaleNumber(Boolean highScaleNumber) {
        this.highScaleNumber = highScaleNumber;
    }

    /**
     *
     * @return
     * The htmlFormatted
     */
    public Boolean getHtmlFormatted() {
        return htmlFormatted;
    }

    /**
     *
     * @param htmlFormatted
     * The htmlFormatted
     */
    public void setHtmlFormatted(Boolean htmlFormatted) {
        this.htmlFormatted = htmlFormatted;
    }

    /**
     *
     * @return
     * The idLookup
     */
    public Boolean getIdLookup() {
        return idLookup;
    }

    /**
     *
     * @param idLookup
     * The idLookup
     */
    public void setIdLookup(Boolean idLookup) {
        this.idLookup = idLookup;
    }

    /**
     *
     * @return
     * The inlineHelpText
     */
    public Object getInlineHelpText() {
        return inlineHelpText;
    }

    /**
     *
     * @param inlineHelpText
     * The inlineHelpText
     */
    public void setInlineHelpText(Object inlineHelpText) {
        this.inlineHelpText = inlineHelpText;
    }

    /**
     *
     * @return
     * The label
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     * The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     * The length
     */
    public Integer getLength() {
        return length;
    }

    /**
     *
     * @param length
     * The length
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     *
     * @return
     * The mask
     */
    public Object getMask() {
        return mask;
    }

    /**
     *
     * @param mask
     * The mask
     */
    public void setMask(Object mask) {
        this.mask = mask;
    }

    /**
     *
     * @return
     * The maskType
     */
    public Object getMaskType() {
        return maskType;
    }

    /**
     *
     * @param maskType
     * The maskType
     */
    public void setMaskType(Object maskType) {
        this.maskType = maskType;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The nameField
     */
    public Boolean getNameField() {
        return nameField;
    }

    /**
     *
     * @param nameField
     * The nameField
     */
    public void setNameField(Boolean nameField) {
        this.nameField = nameField;
    }

    /**
     *
     * @return
     * The namePointing
     */
    public Boolean getNamePointing() {
        return namePointing;
    }

    /**
     *
     * @param namePointing
     * The namePointing
     */
    public void setNamePointing(Boolean namePointing) {
        this.namePointing = namePointing;
    }

    /**
     *
     * @return
     * The nillable
     */
    public Boolean getNillable() {
        return nillable;
    }

    /**
     *
     * @param nillable
     * The nillable
     */
    public void setNillable(Boolean nillable) {
        this.nillable = nillable;
    }

    /**
     *
     * @return
     * The permissionable
     */
    public Boolean getPermissionable() {
        return permissionable;
    }

    /**
     *
     * @param permissionable
     * The permissionable
     */
    public void setPermissionable(Boolean permissionable) {
        this.permissionable = permissionable;
    }

    /**
     *
     * @return
     * The picklistValues
     */
    public List<PicklistValue> getPicklistValues() {
        return picklistValues;
    }

    /**
     *
     * @param picklistValues
     * The picklistValues
     */
    public void setPicklistValues(List<PicklistValue> picklistValues) {
        this.picklistValues = picklistValues;
    }

    /**
     *
     * @return
     * The precision
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     *
     * @param precision
     * The precision
     */
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    /**
     *
     * @return
     * The queryByDistance
     */
    public Boolean getQueryByDistance() {
        return queryByDistance;
    }

    /**
     *
     * @param queryByDistance
     * The queryByDistance
     */
    public void setQueryByDistance(Boolean queryByDistance) {
        this.queryByDistance = queryByDistance;
    }

    /**
     *
     * @return
     * The referenceTargetField
     */
    public Object getReferenceTargetField() {
        return referenceTargetField;
    }

    /**
     *
     * @param referenceTargetField
     * The referenceTargetField
     */
    public void setReferenceTargetField(Object referenceTargetField) {
        this.referenceTargetField = referenceTargetField;
    }

    /**
     *
     * @return
     * The referenceTo
     */
    public List<String> getReferenceTo() {
        return referenceTo;
    }

    /**
     *
     * @param referenceTo
     * The referenceTo
     */
    public void setReferenceTo(List<String> referenceTo) {
        this.referenceTo = referenceTo;
    }

    /**
     *
     * @return
     * The relationshipName
     */
    public Object getRelationshipName() {
        return relationshipName;
    }

    /**
     *
     * @param relationshipName
     * The relationshipName
     */
    public void setRelationshipName(Object relationshipName) {
        this.relationshipName = relationshipName;
    }

    /**
     *
     * @return
     * The relationshipOrder
     */
    public Object getRelationshipOrder() {
        return relationshipOrder;
    }

    /**
     *
     * @param relationshipOrder
     * The relationshipOrder
     */
    public void setRelationshipOrder(Object relationshipOrder) {
        this.relationshipOrder = relationshipOrder;
    }

    /**
     *
     * @return
     * The restrictedDelete
     */
    public Boolean getRestrictedDelete() {
        return restrictedDelete;
    }

    /**
     *
     * @param restrictedDelete
     * The restrictedDelete
     */
    public void setRestrictedDelete(Boolean restrictedDelete) {
        this.restrictedDelete = restrictedDelete;
    }

    /**
     *
     * @return
     * The restrictedPicklist
     */
    public Boolean getRestrictedPicklist() {
        return restrictedPicklist;
    }

    /**
     *
     * @param restrictedPicklist
     * The restrictedPicklist
     */
    public void setRestrictedPicklist(Boolean restrictedPicklist) {
        this.restrictedPicklist = restrictedPicklist;
    }

    /**
     *
     * @return
     * The scale
     */
    public Integer getScale() {
        return scale;
    }

    /**
     *
     * @param scale
     * The scale
     */
    public void setScale(Integer scale) {
        this.scale = scale;
    }

    /**
     *
     * @return
     * The soapType
     */
    public String getSoapType() {
        return soapType;
    }

    /**
     *
     * @param soapType
     * The soapType
     */
    public void setSoapType(String soapType) {
        this.soapType = soapType;
    }

    /**
     *
     * @return
     * The sortable
     */
    public Boolean getSortable() {
        return sortable;
    }

    /**
     *
     * @param sortable
     * The sortable
     */
    public void setSortable(Boolean sortable) {
        this.sortable = sortable;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The unique
     */
    public Boolean getUnique() {
        return unique;
    }

    /**
     *
     * @param unique
     * The unique
     */
    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    /**
     *
     * @return
     * The updateable
     */
    public Boolean getUpdateable() {
        return updateable;
    }

    /**
     *
     * @param updateable
     * The updateable
     */
    public void setUpdateable(Boolean updateable) {
        this.updateable = updateable;
    }

    /**
     *
     * @return
     * The writeRequiresMasterRead
     */
    public Boolean getWriteRequiresMasterRead() {
        return writeRequiresMasterRead;
    }

    /**
     *
     * @param writeRequiresMasterRead
     * The writeRequiresMasterRead
     */
    public void setWriteRequiresMasterRead(Boolean writeRequiresMasterRead) {
        this.writeRequiresMasterRead = writeRequiresMasterRead;
    }

}
