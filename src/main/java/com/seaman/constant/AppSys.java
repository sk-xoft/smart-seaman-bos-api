package com.seaman.constant;

public class AppSys {

    // Global
    public static final String APPLICATION_NAME = "smart-seaman-bos-api";
    public static final String APPLICATION_DESC = "Smart Seaman BOS API.";
    public static final String APPLICATION_VERSION = "V 0.1";
    public static final String LANGUAGE = "language";
    public static final String LANG_EN = "EN";
    public static final String LANG_TH = "TH";

    public static final String REQUEST_BODY = "requestBody";
    public static final String RESPONSE_BODY = "responseBody";

    // Interceptor
    public static final String  API_EXECUTIME  =  "executime";
    public static final String  TRACE_ID =  "trace_id";

    public static final String HEADER_CORRELATION_ID = "correlationid";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_LANGUAGE = "language";
    public static final String HEADER_DEVICE_MODEL = "devicemodel";
    public static final String HEADER_DEVICE_INFO =  "deviceinfo";

    public static final String HEADER_TOKEN = "token";

    public static final String CLIENT_IP = "client_ip";


    /** JWT Payload **/
    public static final String CLAIMS_ISSUER = "iss";       // Issuer
    public static final String CLAIMS_SUBJECT = "sub";      // Subject
    public static final String CLAIMS_AUDIENCE = "aud";     // Audience
    public static final String CLAIMS_EXPIRATION = "exp";   // Expiration
    public static final String CLAIMS_NOT_BEFORE = "nbf";   // Not Before
    public static final String CLAIMS_ISSUED_AT = "iat";    // Issued At
    public static final String CLAIMS_JTI = "jti";          // JWT ID


    public static final String NOTI_TYPE_NEWS_GENERAL = "NEWS_GENERAL";
    public static final String NOTI_TYPE_NEWS_SHIP = "NEWS_SHIP";
    public static final String NOTI_TYPE_CERT_UPDATED = "CERT_UPDATED";
    public static final String NOTI_TYPE_CERT_DELETE = "CERT_DELETED";
    public static final String NOTI_TYPE_VOUCHER =  "VOUCHER";

}
