/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
<<<<<<< HEAD
 * Version 2.0.8
=======
 * Version 2.0.7
>>>>>>> 35d3953b6e4a313efd8093865cbc3314438cd8eb
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.lincomengineering.lincomroip;

public class lincomroip {
  public static int initLincomRoIP(String registrar, String user, String passwd) {
    return lincomroipJNI.initLincomRoIP(registrar, user, passwd);
  }

  public static int destroyLincomRoIP() {
    return lincomroipJNI.destroyLincomRoIP();
  }

  public static int connectLincomRoIP(String s) {
    return lincomroipJNI.connectLincomRoIP(s);
  }

  public static int disconnectLincomRoIP() {
    return lincomroipJNI.disconnectLincomRoIP();
  }

  public static int sendmsgLincomRoIP(int call_id, String s) {
    return lincomroipJNI.sendmsgLincomRoIP(call_id, s);
  }

}