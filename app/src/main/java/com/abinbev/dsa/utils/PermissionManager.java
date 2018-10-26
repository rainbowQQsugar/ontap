package com.abinbev.dsa.utils;

import com.abinbev.dsa.model.OnTap_Permission__c;
import com.abinbev.dsa.model.Profile;
import com.abinbev.dsa.model.User;
import com.salesforce.androidsdk.accounts.UserAccountManager;

/**
 * Singleton used to validate the user's permissions
 */
public class PermissionManager {

    private static PermissionManager instance;

    private OnTap_Permission__c userPermissions;

    private PermissionManager(OnTap_Permission__c userPermissions) {
        this.userPermissions = userPermissions;
    }

    public static PermissionManager getInstance() {
        if (instance == null) {
            synchronized (PermissionManager.class) {
                if (instance == null) {
                    instance = new PermissionManager(loadPermissions());
                }
            }
        }
        return instance;
    }

    /**
     * Method reloads permissions and stores them to current singleton instance (if exists).
     */
    public static void refreshInstance() {
        if (instance != null) {
            instance.setPermissions(loadPermissions());
        }
    }

    private static OnTap_Permission__c loadPermissions() {
        String userId = UserAccountManager.getInstance().getStoredUserId();
        String profileId = User.getUserByUserId(userId).getProfileId();
        Profile profile = Profile.getProfileById(profileId);
        if (profile != null) {
            return OnTap_Permission__c.getPermissionByProfileName(profile.getName());
        } else {
            return null;
        }
    }

    private void setPermissions(OnTap_Permission__c userPermissions) {
        this.userPermissions = userPermissions;
    }

    /**
     * Checks if the current users has the appropriate permission
     * <p>
     * Defaults to 'true' if the user's profile does not specifically
     * restrict the permission.
     *
     * @param permission
     * @return
     */
    public boolean hasPermission(int permission) {
        if (userPermissions == null) {
            return true; //very scary for a security model, but is based on backend impl.
        }

        switch (permission) {
            case VISIT_LIST:
                return userPermissions.hasPermissionForVisitList();
            case PROSPECT_LIST:
                return userPermissions.hasPermissionForProspectList();
            case ACCOUNTS:
                return userPermissions.hasPermissionForAccountList();
            case QUIZZES:
                return userPermissions.hasPermissionForQuizzes();
            case DSA:
                return userPermissions.hasPermissionForDSA();
            case CHATTER:
                return userPermissions.hasPermissionForChatter();
            case CALCULATOR:
                return userPermissions.hasPermissionForCalculator();
            case FULL_SYNC:
                return userPermissions.hasPermissionForFullSync();
            case POCE_TILE:
                return userPermissions.hasPermissionForPOCE();
            case SURVEYS_TILE:
                return userPermissions.hasPermissionForSurveys();
            case TASKS_TILE:
                return userPermissions.hasPermissionForTasks();
            case PROMOTIONS_TILE:
                return userPermissions.hasPermissionForPromotions();
            case CASES_TILE:
                return userPermissions.hasPermissionForCases();
            case NEGOTIATIONS_TILE:
                return userPermissions.hasPermissionForNegotiations();
            case ORDERS_TILE:
                return userPermissions.hasPermissionForOrders();
            case ASSETS_TILE:
                return userPermissions.hasPermissionForAssets();
            case CREATE_SURVEY:
                return userPermissions.hasPermissionForCreateSurvey();
            case CREATE_NEGOTIATION:
                return userPermissions.hasPermissionForCreateNegotiation();
            case CREATE_TASKS:
                return userPermissions.hasPermissionForCreateTasks();
            case CREATE_ASSETS:
                return userPermissions.hasPermissionForCreateAssets();
            case CREATE_ORDER:
                return userPermissions.hasPermissionForCreateOrders();
            case CREATE_CASES:
                return userPermissions.hasPermissionForCreateCases();
            case MARKET_PROGRAMS_TILE:
                return userPermissions.hasPermissionForMarketPrograms();
            case NEGOTIATION_GAUGE:
                return userPermissions.hasPermissionForNegotiationGauge();
            case PROSPECT_BASIC_DATA:
                return userPermissions.hasPermissionForProspectBasicData();
            case PROSPECT_ADDITIONAL_DATA:
                return userPermissions.hasPermissionForProspectAdditionalData();
            case PROSPECT_NEGOTIATIONS:
                return userPermissions.hasPermissionForProspectNegotiations();
            case PROSPECT_FILES:
                return userPermissions.hasPermissionForProspectFiles();
            case USER_360:
                return userPermissions.hasPermissionForUser360HamburgerMenu();
            case ACCOUNT_KPIS:
                return userPermissions.hasPermissionForAccountKpis();
            case CHECKOUT_RULES:
                return userPermissions.hasPermissionForCheckoutRules();
            case ASSET_PHOTO_REQUIRED:
                return userPermissions.hasPermissionForCheckoutRules();
            case ASSET_POC_TITLE:
                return userPermissions.hasPermissionForAssetPoc();
            case TECHNICIANS_TITLE:
                return userPermissions.hasPermissionForTechnicians();
            default:
                return false;
        }
    }

    public boolean isMorningMeetingPictureCommentRequired() {
        return userPermissions == null ? true : userPermissions.requiresMorningMeetingPictureComment();
    }

    public boolean isCheckInPictureCommentRequired() {
        return userPermissions == null ? true : userPermissions.requiresCheckInPictureComment();
    }

    private static final double DEFAULT_MINIMAL_MORNING_MEETING_INTERVAL = 12;

    public double getMinimalMorningMeetingInterval() {
        double hrs = userPermissions == null ? DEFAULT_MINIMAL_MORNING_MEETING_INTERVAL :
                userPermissions.getMinimalMorningMeetingInterval();
        return Double.isNaN(hrs) ? DEFAULT_MINIMAL_MORNING_MEETING_INTERVAL : hrs;
    }


    //list of available permissions

    //menu items
    public static final int USER_360 = 0;
    public static final int VISIT_LIST = 1;
    public static final int PROSPECT_LIST = 2;
    public static final int ACCOUNTS = 3;
    public static final int QUIZZES = 4;
    public static final int DSA = 5;
    public static final int CHATTER = 6;
    public static final int CALCULATOR = 7;
    public static final int FULL_SYNC = 8;

    //account 360 tiles
    public static final int POCE_TILE = 9;
    public static final int SURVEYS_TILE = 10;
    public static final int TASKS_TILE = 11;
    public static final int PROMOTIONS_TILE = 12;
    public static final int CASES_TILE = 13;
    public static final int NEGOTIATIONS_TILE = 14;
    public static final int ORDERS_TILE = 15;
    public static final int ASSETS_TILE = 16;
    public static final int MARKET_PROGRAMS_TILE = 17;
    public static final int ACCOUNT_KPIS = 18;

    //create new
    public static final int CREATE_SURVEY = 19;
    public static final int CREATE_NEGOTIATION = 20;
    public static final int CREATE_TASKS = 21;
    public static final int CREATE_ASSETS = 22;
    public static final int CREATE_ORDER = 23;
    public static final int CREATE_CASES = 24;

    //misc permissions
    public static final int NEGOTIATION_GAUGE = 25;

    // prospect permissions
    public static final int PROSPECT_BASIC_DATA = 26;
    public static final int PROSPECT_ADDITIONAL_DATA = 27;
    public static final int PROSPECT_NEGOTIATIONS = 28;
    public static final int PROSPECT_FILES = 29;

    //assets tracking
    public static final int ASSET_PHOTO_REQUIRED = 30;
    //PoC 360 View –asset
    public static final int ASSET_POC_TITLE = 31;
    //PoC 360 View –Technicians
    public static final int TECHNICIANS_TITLE = 32;
    // checkout rule
    public static final int CHECKOUT_RULES = 300;
}
