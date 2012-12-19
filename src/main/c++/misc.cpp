extern "C" {
#include "name_khoobyar_joe_gsspi_win32_SspiBase.h"
}
#include <windows.h>
#include <ntdef.h>
#define SECURITY_WIN32
#include <sspi.h>

JNIEXPORT void JNICALL Java_name_khoobyar_joe_gsspi_win32_SspiBase_init(JNIEnv *env, jobject self) {
	jclass klass = GetObjectClass (env, self);
	SECURITY_STATUS status;

	// Enumerate the available security packages.
	ULONG oLength;
	PSecPkgInfo oPackages;
	status = EnumerateSecurityPackages (&oLength, &oPackages);
	if (status != SEC_E_OK)
		return /* FIXME: throw something */;

	// Set the length field of this instance.
	jlong length = (jlong) oLength;
	jfieldID jLength = GetFieldID (env, klass, "length", "I");
	SetLongField (env, self, jLength, length);

	// Free the packages array before we return.
	FreeContextBuffer (oPackages);
}

//MessageBeep(0);
//MessageBox(NULL, "Beep", "Beeper", MB_OK);
