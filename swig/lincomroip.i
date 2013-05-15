/* File: lincomroip.i */
%module lincomroip

%{
int initLincomRoIP(char *registrar, char *user, char *passwd);
int destroyLincomRoIP();
int connectLincomRoIP(char *s);
int disconnectLincomRoIP();
int sendmsgLincomRoIP(int call_id, char *s);
%}

int initLincomRoIP(char *registrar, char *user, char *passwd);
int destroyLincomRoIP();
int connectLincomRoIP(char *s);
int disconnectLincomRoIP();
int sendmsgLincomRoIP(int call_id, char *s);

