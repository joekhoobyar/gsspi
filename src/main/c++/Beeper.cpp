#include "name_khoobyar_joe_gsspi_Beeper.h"
#include "Windows.h"

JNIEXPORT void JNICALL Java_name_khoobyar_joe_gsspi_Beeper_beep (JNIEnv *, jobject) {
      MessageBeep(0);
      MessageBox(NULL, "Beep", "Beeper", MB_OK);
}
