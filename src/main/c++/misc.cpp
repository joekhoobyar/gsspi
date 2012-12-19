#include "name_khoobyar_joe_gsspi_win32_SspiBase.h"
#include "Windows.h"

JNIEXPORT jboolean JNICALL Java_name_khoobyar_joe_gsspi_win32_SspiBase_init(JNIEnv *env, jobject self) {
	SECURITY_STATUS status;
	ULONG oLength;
	PSecPkgInfo oPackages;

	status = EnumerateSecurityPackages (&oLength, &oPackages);
	if (status != SEC_E_OK)
		return JNI_FALSE;
	SetLongField (env, self, (jlong) oLength);
	FreeContextBuffer (oPackages);
	return JNI_TRUE;
}

//MessageBeep(0);
//MessageBox(NULL, "Beep", "Beeper", MB_OK);
