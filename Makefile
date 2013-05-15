include ../../../build.mak
include $(PJDIR)/build/common.mak

NDKROOT = /home/alex/android-ndk-r8b

RULES_MAK := $(PJDIR)/build/rules.mak

MYCC := ${NDKROOT}/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-gcc
MYCXX := ${NDKROOT}/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-g++

MYCXXFLAGS = -frtti -I${NDKROOT}/platforms/android-9/arch-arm/usr/include  -shared --sysroot=${NDKROOT}/platforms/android-9/arch-arm
MYINC = -L/home/vangelov/pjandroid/android/pjlib/lib -L/home/vangelov/pjandroid/android/pjlib-util/lib -L/home/vangelov/pjandroid/android/pjnath/lib -L/home/vangelov/pjandroid/android/pjmedia/lib -L/home/vangelov/pjandroid/android/pjsip/lib -L/home/vangelov/pjandroid/android/third_party/lib
MYLDFLAGS = -nostdlib -L${NDKROOT}/platforms/android-9/arch-arm/usr/lib/
MYCFLAGS = -frtti -I${NDKROOT}/platforms/android-9/arch-arm/usr/include -DPJ_IS_BIG_ENDIAN=0 -DPJ_IS_LITTLE_ENDIAN=1
SWIG=swig

all: lincomroip_wrap.o
	${MYCXX} ${APP_CXXFLAGS} ${APP_LDFLAGS} -o libs/armeabi/liblincomroip.so swig/lincomroip_wrap.o swig/lincomroip.o ${APP_LDLIBS}

all_bug: bug
	${MYCXX} ${APP_CXXFLAGS} ${APP_LDFLAGS} -o libs/armeabi/liblincomroip.so swig/lincomroip_wrap.o swig/lincomroip.o ${APP_LDLIBS}
bug: 
	${MYCC} -c swig/lincomroip_wrap.cxx ${MYCFLAGS} -o swig/lincomroip_wrap.o
	${MYCC} -c swig/lincomroip.cxx ${APP_CFLAGS} -o swig/lincomroip.o

lincomroip_wrap.o: lincomroip.i
	${MYCC} -c swig/lincomroip_wrap.cxx ${MYCFLAGS} -o swig/lincomroip_wrap.o
	${MYCC} -c swig/lincomroip.cxx ${APP_CFLAGS} -o swig/lincomroip.o

lincomroip.i: swig/lincomroip.i
	${SWIG} -c++ -package com.lincomengineering.lincomroip -outdir src/com/lincomengineering/lincomroip -java swig/lincomroip.i
