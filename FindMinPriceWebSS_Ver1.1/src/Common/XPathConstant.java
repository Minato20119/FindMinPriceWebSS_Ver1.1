/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Common;

/**
 *
 * @author Minato
 */
public class XPathConstant {

    public static final class REGEX {

        public static final String REGEX_GET_CODE_OF_PRODUCT = "[0-9a-z]+\\s[0-9a-z]+$";
        public static final String REGEX_SEARCH_PRODUCT_IN_WEB = "\\s[a-zA-Z0-9]+(\\s+\\S+){1,2}$";
        public static final String REGEX_GET_BLOCK_CONTAINS_PRICE = "<h3 class=\"title[\\s|\\S]*?<\\/div>";
        public static final String REGEX_GET_TITLE_OF_PRODUCT = "(target=\"_blank\">)(.+)(<\\/a><\\/h3>)";
        public static final String REGEX_GET_PRICE_IN_BLOCK = "\\s([0-9.+]+)(\\sđ)\\s";
        public static final String REGEX_GET_NUMBER_PAGES = "(data-page-index=\")([0-9]+)";
    }
    
}
