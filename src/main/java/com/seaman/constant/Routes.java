package com.seaman.constant;

public class Routes {

    public static final String VERSION =  "/v1";
    public static final String HEALTH = "/health";
    public static final String LOGIN = "/login";
    public static final String REFRESH_TOKEN = "/refresh-token";
    public static final String REGISTER = "/register";
    public static final String PROFILE = "/profile";
    public static final String PROFILE_PICTURE = "/profile/pic";
    public static final String MASTER = "/master";
    public static final String MASTER_SCHOOLS = "/master/schools";
    public static final String MASTER_COMPANYS = "/master/companys";
    public static final String MASTER_COURSE_NAME= "/master/course-name";
    public static final String MASTER_GROUP_NAME= "/master/group-name";
    public static final String MASTER_MENU= "/master/menu";
    public static final String CERTIFICATE = "/certificate";
    public static final String MENU = "/list-menu";
    public static final String AUTHORISED = "/get-authorised";

    public static final String INTERCEPTOR = "/interceptor";
    public static final String ALLCOURSE = "/list-course";
    public static final String LIST_COURSE_BY_ID = "/list-course/{id}";
    public static final String CREATE_COURSE = "/course/add";
    public static final String DELETE_COURSE = "/course/delete";
    public static final String UPDATE_COURSE = "/course/update";

    public static final String LIST_FORM = "/list-form";
    public static final String CREATE_FORM = "/form/add";
    public static final String UPDATE_FORM = "/form/update";
    public static final String DELETE_FORM = "/form/delete";
    public static final String LIST_FORM_BY_ID = "/list-form/{id}";
    public static final String FORM_DOWNLOAD = "/from-download";

    public static final String LIST_NEWS = "/list-news";

    public static final String CREATE_NEWS = "/news/add";
    public static final String UPDATE_NEWS = "/news/update";
    public static final String DELETE_NEWS = "/news/delete";
    public static final String LIST_NEWS_BY_ID = "/list-news/{id}";
    public static final String SEND_NOTI_NEWS = "/send-noti/news";

    public static final String PREVIEW_NEWS = "/news/preview/{id}";

    public static final String ADMIN = "/list-admin";
    public static final String LIST_ADMIN_BY_ID ="/list-admin/{id}";
    public static final String CREATE_ADMIN = "/admin/add";
    public static final String UPDATE_ADMIN = "/admin/update";
    public static final String DELETE_ADMIN = "/admin/delete";
    public static final String UPDATE_PASSWORD = "/admin/update-password";

    public static final String LIST_BANNER = "/list-banner";
    public static final String CREATE_BANNER= "/banner/add";
    public static final String DELETE_BANNER = "/banner/delete";
    public static final String PREVIEW_BANNER =  "/banner/preview/{id}";
    public static final String USER_MOBILE_LIST = "/user-mobile/list";
    public static final String USER_MOBILE_PROFILE = "/user-mobile/profile";
    public static final String USER_MOBILE_PROFILE_PIC = "/user-mobile/profile/image";
    public static final String CERT_TYPE_COT = "/cert-type-cot";
    public static final String CERT_TYPE_DOCUMENT = "/cert-type-document";
    public static final String CERT_USER_MOBILE_PREVIEW = "/cert-user-preview";
    public static final String MASTER_DOCUMENTS = "/master/documents";
    public static final String MASTER_COMPANY = "/master/company";

    public static final String CREATE_CERT = "/documents/certification/create";
    public static final String UPDATE_CERT = "/documents/certification/update";
    public static final String DELETE_CERT = "/documents/certification/delete";
    public static final String EDIT_CERT = "/documents/certification/edit";

    public static final String ALL_USER = "/get-all-user";
    public static final String ALL_SMART_CODE = "/smartseaman-code-list";
    public static final String LIST_USER_MOBILE = "/list-mobile-user/{id}";

    public static final String LESS_THAN_THREE = "/get-less-three";

    public static final String LESS_THAN_SIX = "/get-less-six";

    public static final String LESS_THAN_YEAR = "/get-less-year";

    public static final String VOUCHERS = "/vouchers";

    public static final String VOUCHERS_CREATE = "/vouchers/add";
    public static final String VOUCHERS_UPDATE = "/vouchers/update";
    public static final String VOUCHERS_DELETE = "/vouchers/delete";
    public static final String VOUCHERS_BY_ID = "/vouchers/detail/{id}";


    public static final String LIST_GROUP = "/list-group";
    public static final String LIST_GROUP_BY_ID ="/list-group/{id}";
    public static final String CREATE_GROUP = "/group/add";
    public static final String CREATE_GROUP_ROLE = "/group/add-detail";
    public static final String UPDATE_GROUP = "/group/update";
    public static final String DELETE_GROUP = "/group/delete";
}
