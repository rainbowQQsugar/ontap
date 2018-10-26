package com.salesforce.androidsyncengine.data.model;

import com.salesforce.androidsyncengine.syncmanifest.FieldValueObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bduggirala on 11/16/15.
 */
public class PicklistDependencyHolder extends HashMap<FieldValueObject, List<FieldValueObject>> implements Serializable {

    private static final long serialVersionUID = 1L;
}
