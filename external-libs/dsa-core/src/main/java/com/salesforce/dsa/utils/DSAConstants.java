package com.salesforce.dsa.utils;

public interface DSAConstants {

    interface Constants {
        String ACTIVE_CONFIG_ID = "Active_Config_Id";
        String INTERNAL_MODE = "Internal_Mode";
        String PRIVATE_FILE_PUBLISH_STATUS = "R";
    }

    interface Formats {
        String SQL_FORMAT = "SELECT {%1$s:%2$s} FROM {%1$s} WHERE %3$s";
        String SMART_SQL_FORMAT = "SELECT {%1$s:_soup} FROM {%1$s} WHERE %2$s";
        String SMART_SQL_FETCH_ID_FORMAT = "SELECT {%1$s:Id} FROM {%1$s} WHERE %2$s";
        String SMART_SQL_ALL_FORMAT = "SELECT {%1$s:_soup} FROM {%1$s}";
        String SMART_SQL_JOIN_FORMAT = "SELECT {%1$s:_soup}, {%2$s:_soup} FROM {%1$s} "
                + "LEFT JOIN {%2$s} ON {%1$s:%3$s} = {%2$s:%4$s} WHERE %5$s";
        String SMART_SQL_JOIN_ALL_FORMAT = "SELECT {%1$s:_soup}, {%2$s:_soup} FROM {%1$s} "
                + "LEFT JOIN {%2$s} ON {%1$s:%3$s} = {%2$s:%4$s}";
    }

    interface DSAObjects {
        String CATEGORY = "Category__c";
        String MOBILE_APP_CONFIG = "MobileAppConfig__c";
        String CATEGORY_MOBILE_CONFIG = "CategoryMobileConfig__c";
        String CONTENT_VERSION = "ContentVersion";
        String CAT_CONTENT_JUNCTION = "Cat_Content_Junction__c";
        String CONTENT_DOCUMENT = "ContentDocument";
        String ATTACHMENT = "Attachment";
        String CONTACT = "Contact";
        String EVENT = "Event";
        String DSA_PLAYLIST = "DSA_Playlist__c";
        String PLAYLIST_CONTENT_JUNCTION = "Playlist_Content_Junction__c";
        String USER = "User";
        String ACCOUNT = "Account";
        String KPI = "KPI__c";
        String CN_DSA_Folder__c = "CN_DSA_Folder__c";

    }

    interface MobileAppConfigFields {
        String ACTIVE = "Active__c";
        String BUTTON_DEFAULT_ATTACHMENT_ID = "ButtonDefaultAttachmentId__c";
        String BUTTON_HIGHLIGHT_ATTACHMENT_ID = "ButtonHighlightAttachmentId__c";
        String BUTTON_HIGHLIGHT_TEXT_COLOR = "ButtonHighlightTextColor__c";
        String BUTTON_TEXT_ALPHA = "ButtonTextAlpha__c";
        String BUTTON_TEXT_COLOR = "ButtonTextColor__c";
        String CHECK_IN_ENABLED = "Check_In_Enabled__c";
        String INTRO_TEXT_ALPHA = "IntroTextAlpha__c";
        String INTRO_TEXT_COLOR = "IntroTextColor__c";
        String INTRO_TEXT = "IntroText__c";
        String LANDSCAPE_ATTACHMENT_ID = "LandscapeAttachmentId__c";
        String LANGUAGE = "Language__c";
        String LINK_TO_EDITOR = "LinkToEditor__c";
        String LOGO_ATTACHMENT_ID = "LogoAttachmentId__c";
        String PORTRAIT_ATTACHMENT_ID = "PortraitAttachmentId__c";
        String PROFILE_TEXT = "ProfileText__c";
        String PROFILE_NAMES = "Profile_Names__c";
        String PROFILES = "Profiles__c";
        String REPORT_AN_ISSUE = "Report_an_Issue__c";
        String TITLE_BG_ALPHA = "TitleBgAlpha__c";
        String TITLE_BG_COLOR = "TitleBgColor__c";
        String TITLE_TEXT_ALPHA = "TitleTextAlpha__c";
        String TITLE_TEXT_COLOR = "TitleTextColor__c";
        String TITLE_TEXT = "TitleText__c";
        String IN_EDIT = "inEdit__c";
    }

    interface CategoryMobileConfigFields {
        String LAST_REFERENCE_DATE = "LastReferencedDate";
        String LAST_VIEWED_DATE = "LastViewedDate";
        String BUTTON_TEXT_ALIGN = "Button_Text_Align__c";
        String CATEGORY_BUNDLE_ID = "CategoryBundleId__c";
        String CATEGORY_ID = "CategoryId__c";
        String CONTENT_ATTACHMENT_ID = "ContentAttachmentId__c";
        String CONTENT_OVER_ATTACHMENT_ID = "ContentOverAttachmentId__c";
        String GALLERY_HEADING_TEXT = "GalleryHeadingText__c";
        String GALLERY_HEADING_TEXT_COLOR = "GalleryHeadingTextColor__c";
        String IS_DEFAULT = "IsDefault__c";
        String IS_DRAFT = "IsDraft__c";
        String LANDSCAPE_ATTACHMENT_ID = "LandscapeAttachmentId__c";
        String LANDSCAPE_X = "LandscapeX__c";
        String LANDSCAPE_Y = "LandscapeY__c";
        String MAC_IN_EDIT = "MAC_in_Edit__c";
        String MOBILE_APP_CONFIGURATION_ID = "MobileAppConfigurationId__c";
        String OVERLAY_BG_ALPHA = "OverlayBgAlpha__c";
        String OVERLAY_BG_COLOR = "OverlayBgColor__c";
        String OVERLAY_TEXT_COLOR = "OverlayTextColor__c";
        String PORTRAIT_ATTACHMENT_ID = "PortraitAttachmentId__c";
        String PORTRAIT_X = "PortraitX__c";
        String PORTRAIT_Y = "PortraitY__c";
        String SUB_CATEGORY_BACKGROUND_COLOR = "Sub_Category_Background_Color__c";
        String TOP_LEVEL_CATEGORY = "Top_Level_Category__c";
        String USE_CATEGORY_BUNDLE = "UseCategoryBundle__c";
    }

    interface CategoryFields {
        String DESCRIPTION = "Description__c";
        String GALLERY_ATTACHMENT_ID = "GalleryAttachmentId__c";
        String IS_PARENT_CATEGORY = "Is_Parent_Category__c";
        String IS_PARENT = "Is_Parent__c";
        String IS_TOP_LEVEL = "Is_Top_Level__c";
        String LANGUAGE = "Language__c";
        String ORDER = "Order__c";
        String PARENT_CATEGORY = "Parent_Category__c";
        String TODAYS_SPECIAL = "Todays_Special__c";
    }

    interface ContentVersionFields {
        String CONTENT_DOCUMENT_ID = "ContentDocumentId";
        String DESCRIPTION = "Description";
        String TITLE = "Title";
        String PATH_ON_CLIENT = "PathOnClient";
        String CONTENT_URL = "ContentUrl";
        String FILE_TYPE = "FileType";
        String FEATURED_CONTENT_BOOST = "FeaturedContentBoost";
        String TAGS_CSV = "TagCsv";
        String CONTENT_MODIFIED_DATE = "ContentModifiedDate";
        String CONTENT_SIZE = "contentSize";
        String VERSION_NUMBER = "VersionNumber";
        String DOCUMENT_TYPE = "Document_Type__c";
        String CATEGORY = "Category__c";
        String VERSION_DATA = "VersionData";
        String INTERNAL_DOCUMENT = "Internal_Document__c";
        String PUBLISH_STATUS = "PublishStatus";
    }

    interface CatContentJunctionFields {
        String IS_DELETED = "IsDeleted";
        String LAST_ACTIVITY_DATE = "LastActivityDate";
        String LAST_REFERENCED_DATE = "LastReferencedDate";
        String LAST_VIEWED_DATE = "LastViewedDate";
        String CATEGORY__C = "Category__c";
        String CONTENTID__C = "ContentId__c";
        String EXTERNAL_ID__C = "External_Id__c";
    }

    interface ContentDocumentFields {
        String ARCHIVED_BY_ID = "ArchivedById";
        String ARCHIVED_DATE = "ArchivedDate";
        String IS_ARCHIVED = "IsArchived";
        String IS_DELETED = "IsDeleted";
        String LAST_REFERENCED_DATE = "LastReferencedDate";
        String LAST_VIEWED_DATE = "LastViewedDate";
        String LATEST_PUBLISHED_VERSION_ID = "LatestPublishedVersionId";
        String PARENT_ID = "ParentId";
        String PUBLISH_STATUS = "PublishStatus";
        String TITLE = "Title";
    }

    interface AttachmentFields {
        String BODY = "Body";
        String BODY_LENGTH = "BodyLength";
        String CONTENT_TYPE = "ContentType";
        String DESCRIPTION = "Description";
        String IS_PRIVATE = "IsPrivate";
        String PARENT_ID = "ParentId";
    }

    interface ContactFields {
        String FIRST_NAME = "FirstName";
        String LAST_NAME = "LastName";
        String EMAIL = "Email";
        String ACCOUNT_ID = "AccountId";
        String ACCOUNT = "Account";
        String BIRTHDATE = "Birthdate";
    }

    interface UserFields {
        String NAME = "Name";
    }

    interface PlaylistFields {
        String NAME = "Name";
        String IS_FEATURED = "IsFeatured__c";
    }

    interface PlaylistContentJunctionFields {
        String CONTENT_ID = "ContentId__c";
        String PLAYLIST_ID = "Playlist__c";
        String ORDER = "Order__c";
    }

    interface AccountFields {
        String NAME = "Name";
        String PHONE = "Phone";
        String PHONE_OTHER = "PersonOtherPhone";
    }

    interface EventFields {
        String END_DATE_TIME = "EndDateTime";
        String ACCOUNT_ID = "AccountId";
        String ACTIVITY_DATE = "ActivityDate";
        String START_DATE_TIME = "StartDateTime";
    }


    interface CNDSAFolderFields {
        String CN_Parent_Folder__c = "CN_Parent_Folder__c";
        String CN_DSA__c = "CN_DSA__c";
        String CN_Is_Top_Level__c = "CN_Is_Top_Level__c";
    }

    interface CNDSAFields {
        String OwnerID = "OwnerID";
        String IsActive__c = "IsActive__c";
    }

    interface CNShareFields {
        String CN_Share__c = "CN_Share__c";
        String CN_Parent_ID__c = "CN_Parent_ID__c";
    }
}
