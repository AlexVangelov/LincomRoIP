#include <pjsua-lib/pjsua.h>
#include <android/log.h>
#include <jni.h>

#define THIS_FILE	"LincomRoIP"

static void callback_handler(char *s);
int sendmsgLincomRoIP(int call_id, char *s);

/* Callback called by the library upon receiving incoming call */
static void on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id,
			     pjsip_rx_data *rdata)
{
    pjsua_call_info ci;

    PJ_UNUSED_ARG(acc_id);
    PJ_UNUSED_ARG(rdata);

    pjsua_call_get_info(call_id, &ci);

    PJ_LOG(3,(THIS_FILE, "Incoming call from %.*s!!",
			 (int)ci.remote_info.slen,
			 ci.remote_info.ptr));

    /* Automatically answer incoming calls with 200/OK */
    pjsua_call_answer(call_id, 200, NULL, NULL);
}

/* Callback called by the library when call's state has changed */
static void on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    pjsua_call_info ci;

    PJ_UNUSED_ARG(e);

    pjsua_call_get_info(call_id, &ci);
    PJ_LOG(3,(THIS_FILE, "Call %d state=%.*s", call_id,
			 (int)ci.state_text.slen,
			 ci.state_text.ptr));
		if (ci.state == PJSIP_INV_STATE_CONFIRMED) sendmsgLincomRoIP(call_id,"?");
}

/* Callback called by the library when call's media state has changed */
static void on_call_media_state(pjsua_call_id call_id)
{
    pjsua_call_info ci;

    pjsua_call_get_info(call_id, &ci);

    if (ci.media_status == PJSUA_CALL_MEDIA_ACTIVE) {
	// When media is active, connect call to sound device.
	pjsua_conf_connect(ci.conf_slot, 0);
	pjsua_conf_connect(0, ci.conf_slot);
			//sendmsgLincomRoIP(call_id,"?");
    }
}

static void on_pager(pjsua_call_id call_id, const pj_str_t *from, 
                     const pj_str_t *to, const pj_str_t *contact,
                     const pj_str_t *mime_type, const pj_str_t *text)
{
    /* Note: call index may be -1 */
    PJ_UNUSED_ARG(to);
    PJ_UNUSED_ARG(contact);
    PJ_UNUSED_ARG(mime_type);

    PJ_LOG(3,(THIS_FILE,"MESSAGE from %.*s: %.*s (%.*s)",
              (int)from->slen, from->ptr,
              (int)text->slen, text->ptr,
              (int)mime_type->slen, mime_type->ptr)); 

   if (strncmp (text->ptr,"ICOM:",5) == 0) callback_handler(text->ptr+5); //pager_cb(text->ptr+5);
}

void printlog(const char *format, ...)
{
    va_list arg;
    
    va_start(arg, format);

    __android_log_vprint(ANDROID_LOG_INFO, THIS_FILE, format, arg);

    va_end(arg);
}

void showLog(int level, const char *data, int len)
{
		//toast_cb((char *)data);
	//callback_handler((char *)data);
    printlog("%s", data);
}

int initLincomRoIP(char *registrar, char *user, char *passwd) {
   pjsua_acc_id acc_id;
   pj_status_t status;
   pjsua_transport_id transport_id = -1;
   char errbuf[256];

    /* Create pjsua first! */
   status = pjsua_create();
   if (status != PJ_SUCCESS) {
      printlog("pjsua_create failed ");    
      return -1;
   }

   /* Init pjsua */
   {
      pjsua_config cfg;
      pjsua_logging_config log_cfg;
			pjsua_media_config media_cfg;

      pjsua_config_default(&cfg);
      cfg.cb.on_incoming_call = &on_incoming_call;
      cfg.cb.on_call_media_state = &on_call_media_state;
      cfg.cb.on_call_state = &on_call_state;
      cfg.cb.on_pager = &on_pager;

      pjsua_logging_config_default(&log_cfg);
      log_cfg.console_level = 4;
      log_cfg.cb = &showLog;

			cfg.stun_srv_cnt = 2;
			cfg.stun_srv[0] = pj_str("stun.pjsip.org");
      cfg.stun_srv[1] = pj_str("stun.voipbuster.com");

			pjsua_media_config_default(&media_cfg);
			media_cfg.ec_tail_len = 0;

      status = pjsua_init(&cfg, &log_cfg, &media_cfg);
      if (status != PJ_SUCCESS) {
         printlog("pjsua_init failed ");    
         return -1;
      }
   }

   /* Add UDP transport. */
   {
      pjsua_transport_config cfg;

      pjsua_transport_config_default(&cfg);
      cfg.port = 5060;
      status = pjsua_transport_create(PJSIP_TRANSPORT_UDP, &cfg, &transport_id);
      if (status != PJ_SUCCESS) {
         printlog("pjsua_transport_create failed: %s", pj_strerror(status, errbuf, sizeof(errbuf)));    
         return -1;  
      }
   }

   /* Initialization is done, now start pjsua */
   status = pjsua_start();
   if (status != PJ_SUCCESS)  {
      printlog("pjsua_start failed ");    
      return -1;  
   }

    /* Create local SIP account. */
/*   {
      status = pjsua_acc_add_local(transport_id, PJ_TRUE, &acc_id);
      if (status != PJ_SUCCESS)  {
         printlog("pjsua_acc_add_local");    
         return -1;  
      }
			pjsua_acc_set_online_status(acc_id, PJ_TRUE);
   } */
		//callback_handler((char *)"LincomRoIP initialized!");
		// Register to SIP server by creating account
	{
		char sip_id[64];
		char reg_uri[64];
		pjsua_acc_config cfg;
		pjsua_acc_config_default(&cfg);
		pj_ansi_sprintf(sip_id, "sip:%s@%s",user,registrar);
		cfg.id = pj_str(sip_id);
		pj_ansi_sprintf(reg_uri, "sip:%s",registrar);
		cfg.reg_uri = pj_str(reg_uri);
		cfg.cred_count = 1;
		cfg.cred_info[0].realm = pj_str("*");
		cfg.cred_info[0].scheme = pj_str("digest");
		cfg.cred_info[0].username = pj_str(user);
		cfg.cred_info[0].data_type = PJSIP_CRED_DATA_PLAIN_PASSWD;
		cfg.cred_info[0].data = pj_str(passwd);
		printlog(passwd);  
		status = pjsua_acc_add(&cfg, PJ_TRUE, &acc_id);		
		if (status != PJ_SUCCESS)  {
			 printlog("pjsua_acc_add");    
			 return -1;  
		}
	}
   return 0;
}

int connectLincomRoIP(char *s) {
   pj_status_t status;
   pjsua_call_id call_id = -1;
   pj_str_t uri = pj_str(s);
   pjsua_call_make_call(pjsua_acc_get_default(), &uri, 0, NULL, NULL, &call_id);
   return call_id;
}

int disconnectLincomRoIP() {
	pjsua_call_hangup_all();
}

int sendmsgLincomRoIP(int call_id, char *s) {
	 char amsg[80];
	 pj_ansi_sprintf(amsg, "VCMD:%s",s);
   pj_str_t msg = pj_str(amsg);
   return pjsua_call_send_im( call_id, NULL, &msg, NULL, NULL);
}

int destroyLincomRoIP() {
   pjsua_destroy();
}

static JavaVM *gJavaVM;
static jobject gInterfaceObject, gDataObject;
const char *kInterfacePath = "com/lincomengineering/lincomroip/MainActivity";
static jclass interfaceClass;

void initClassHelper(JNIEnv *env, const char *path, jobject *objptr) {
	jclass cls = env->FindClass(path);
	if(!cls) {
	printlog("initClassHelper: failed to get %s class reference", path);
	return;
	}
	jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
	if(!constr) {
	printlog("initClassHelper: failed to get %s constructor", path);
	return;
	}
	jobject obj = env->NewObject(cls, constr);
	if(!obj) {
	printlog("initClassHelper: failed to create a %s object", path);
	return;
	}
	(*objptr) = env->NewGlobalRef(obj);
}

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	gJavaVM = vm;
	printlog("JNI_OnLoad called");
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		printlog("Failed to get the environment using GetEnv()");
		return -1;
	}
	interfaceClass = env->FindClass(kInterfacePath);
	if(!interfaceClass) {
		printlog("callback_handler: failed to get class reference");
		return -1;
	}
	return JNI_VERSION_1_4;
}

static void callback_handler(char *s) {
	int status;
	JNIEnv *env;
	bool isAttached = false;
	status = gJavaVM->GetEnv((void **) &env, JNI_VERSION_1_4);
	if(status < 0) {
	printlog("callback_handler: failed to get JNI environment, "
	"assuming native thread");
	status = gJavaVM->AttachCurrentThread(&env, NULL);
	if(status < 0) {
	printlog("callback_handler: failed to attach "
	"current thread");
	return;
	}
	isAttached = true;
	}
	/* Construct a Java string */
	jstring js = env->NewStringUTF(s);
	//initClassHelper(env, kInterfacePath, &gInterfaceObject);
	//jclass interfaceClass = env->GetObjectClass(gInterfaceObject);
	//jclass interfaceClass = env->FindClass(kInterfacePath);
	//if(!interfaceClass) {
	//printlog("callback_handler: failed to get class reference");
	//if(isAttached) gJavaVM->DetachCurrentThread();
	//return;
	//}
	/* Find the callBack method ID */
	//jmethodID method = env->GetStaticMethodID(
	jmethodID method = env->GetMethodID(
	interfaceClass, "callBack", "(Ljava/lang/String;)V");
	if(!method) {
	printlog("callback_handler: failed to get method ID");
	if(isAttached) gJavaVM->DetachCurrentThread();
	return;
	}
	env->CallStaticVoidMethod(interfaceClass, method, js);
	if(isAttached) gJavaVM->DetachCurrentThread();
}
