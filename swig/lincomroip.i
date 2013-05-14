/* File: lincomroip.i */
%module lincomroip

%{
int initLincomRoIP();
int destroyLincomRoIP();
int connectLincomRoIP(char *s);
int disconnectLincomRoIP();
int sendmsgLincomRoIP(int call_id, char *s);
%}

int initLincomRoIP();
int destroyLincomRoIP();
int connectLincomRoIP(char *s);
int disconnectLincomRoIP();
int sendmsgLincomRoIP(int call_id, char *s);

