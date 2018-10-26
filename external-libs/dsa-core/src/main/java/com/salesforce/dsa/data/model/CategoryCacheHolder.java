/**
 * The object that holds the Deleted Ids associated with a user account which is the key
 *
 * @author bduggirala@salesforce.com
 */

package com.salesforce.dsa.data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class CategoryCacheHolder extends HashMap<String, List<String>> implements Serializable {

    private static final long serialVersionUID = 1L;

}
