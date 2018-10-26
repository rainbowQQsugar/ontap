package com.salesforce.dsa.app.utils;

import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.dsa.app.R;

/**
 * @author nickc (nick.c@akta.com).
 */
public class FileIconUtil {

    public static int getFileIconThumbnailForFileType(String fileType) {

        if (GuavaUtils.isNullOrEmpty(fileType)) {
            return R.drawable.unknown_thumbnail;
        }

        switch (fileType.toUpperCase()) {
            case "AI":
                return R.drawable.ai_thumbnail;
            case "ATTACHMENT": //TODO: add proper extensions
                return R.drawable.attachment_thumbnail;
            case "AUDIO": //TODO: add proper extensions
                return R.drawable.audio_thumbnail;
            case "CSV":
                return R.drawable.csv_thumbnail;
            case "EPS":
                return R.drawable.eps_thumbnail;
            case "XLS":
            case "XLSX":
            case "XLSM":
            case "EXCEL":
            case "EXCEL_X":
                return R.drawable.excel_thumbnail;
            case "HTML":
                return R.drawable.html_thumbnail;
            case "IMAGE":
            case "PNG":
            case "JPG":
            case "JPEG":
            case "BMP":
                return R.drawable.image_thumbnail;
            case "KEYNOTE":
                return R.drawable.keynote_thumbnail;
            case "MP4":
                return R.drawable.mp4_thumbnail;
            case "PAGES":
                return R.drawable.pages_thumbnail;
            case "PDF":
                return R.drawable.pdf_thumbnail;
            case "PPT":
            case "PPTX":
            case "PPTM":
            case "POWER_POINT":
            case "POWER_POINT_X":
                return R.drawable.ppt_thumbnail;
            case "PSD":
                return R.drawable.psd_thumbnail;
            case "RTF":
                return R.drawable.rtf_thumbnail;
            case "TXT":
                return R.drawable.txt_thumbnail;
            case "URL":
                return R.drawable.url_thumbnail;
            case "VIDEO": //TODO: add proper extensions
                return R.drawable.video_thumbnail;
            case "VSD":
            case "VSS":
            case "VST":
            case "VDX":
            case "VSX":
            case "VTX":
                return R.drawable.visio_thumbnail;
            case "DOC":
            case "DOCM":
            case "DOCX":
            case "WORD":
            case "WORD_X":
                return R.drawable.word_thumbnail;
            case "XML":
                return R.drawable.xml_thumbnail;
            case "ZIP":
                return R.drawable.zip_thumbnail;
            default:
                return R.drawable.unknown_thumbnail;
        }
    }

}
